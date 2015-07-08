package org.robotframework.ide.core.testData.text.context;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.context.recognizer.DeclaredCommentRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.DoubleSpaceOrTabulatorSeparatorRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.EmptyLineRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.IContextRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.KeywordsTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.QuotesSentenceRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.SettingTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.TestCaseTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.VariableTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.CharacterWithByteHexValue;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.CharacterWithShortHexValue;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.LineFeedTextualRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.PipeSeparatorRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.TabulatorTextualRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.UnicodeCharacterWithHexValue;
import org.robotframework.ide.core.testData.text.context.recognizer.variables.CollectionIndexPosition;
import org.robotframework.ide.core.testData.text.context.recognizer.variables.DictionaryVariableRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.variables.EnvironmentVariableRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.variables.ListVariableRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.variables.ScalarVariableRecognizer;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * Main builder of contexts - its responsibility is to invoke context
 * recognizers to get response if current line belongs to them. Next it collects
 * response and define, base on merge logic what context is responsible for this
 * line.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see IContextRecognizer
 * @see DeclaredCommentRecognizer
 * @see QuotesSentenceRecognizer
 * @see SettingTableHeaderRecognizer
 * @see VariableTableHeaderRecognizer
 * @see TestCaseTableHeaderRecognizer
 * @see KeywordsTableHeaderRecognizer
 * @see LineFeedTextualRecognizer
 * @see TabulatorTextualRecognizer
 * @see EmptyLineRecognizer
 * @see CharacterWithByteHexValue
 * @see CharacterWithShortHexValue
 * @see UnicodeCharacterWithHexValue
 * @see ScalarVariableRecognizer
 * @see EnvironmentVariableRecognizer
 * @see ListVariableRecognizer
 * @see DictionaryVariableRecognizer
 * 
 * @see DoubleSpaceOrTabulatorSeparatorRecognizer
 * @see PipeSeparatorRecognizer
 * 
 */
public class ContextBuilder {

    private List<IContextRecognizer> separatorRecognizers = new LinkedList<>();
    private List<IContextRecognizer> normalRecognizers = new LinkedList<>();


    public ContextBuilder() {
        separatorRecognizers
                .add(new DoubleSpaceOrTabulatorSeparatorRecognizer());
        separatorRecognizers.add(new PipeSeparatorRecognizer());
        separatorRecognizers = Collections
                .unmodifiableList(separatorRecognizers);

        normalRecognizers.add(new DeclaredCommentRecognizer());
        normalRecognizers.add(new QuotesSentenceRecognizer());
        normalRecognizers.add(new SettingTableHeaderRecognizer());
        normalRecognizers.add(new VariableTableHeaderRecognizer());
        normalRecognizers.add(new TestCaseTableHeaderRecognizer());
        normalRecognizers.add(new KeywordsTableHeaderRecognizer());

        normalRecognizers.add(new LineFeedTextualRecognizer());
        normalRecognizers.add(new TabulatorTextualRecognizer());
        normalRecognizers.add(new EmptyLineRecognizer());

        normalRecognizers.add(new CharacterWithByteHexValue());
        normalRecognizers.add(new CharacterWithShortHexValue());
        normalRecognizers.add(new UnicodeCharacterWithHexValue());

        normalRecognizers.add(new ScalarVariableRecognizer());
        normalRecognizers.add(new EnvironmentVariableRecognizer());
        normalRecognizers.add(new ListVariableRecognizer());
        normalRecognizers.add(new DictionaryVariableRecognizer());
        normalRecognizers.add(new CollectionIndexPosition());

        normalRecognizers = Collections.unmodifiableList(normalRecognizers);
    }


    public ContextOutput buildContexts(final TokenOutput tokenOutput) {
        ContextOutput out = new ContextOutput(tokenOutput);
        TokensLineIterator lineIter = new TokensLineIterator(tokenOutput);

        while(lineIter.hasNext()) {
            LineTokenPosition lineBoundaries = lineIter.next();
            RobotLineSeparatorsContexts sepCtx = extractSeperators(out,
                    lineBoundaries);
            AggregatedOneLineRobotContexts ctx = new AggregatedOneLineRobotContexts();
            // adding in case some of context will need to see separators
            ctx.setSeparators(sepCtx);
            out.getContexts().add(ctx);

            for (IContextRecognizer recognizer : normalRecognizers) {
                List<IContextElement> recognize = recognizer.recognize(out,
                        lineBoundaries);
                addFoundContexts(ctx, recognize);
            }
        }

        return out;
    }


    private void addFoundContexts(
            final AggregatedOneLineRobotContexts aggregated,
            final List<IContextElement> simpleContexts) {
        if (simpleContexts != null && !simpleContexts.isEmpty()) {
            for (IContextElement ctx : simpleContexts) {
                aggregated.addNextLineContext(ctx);
            }
        }
    }


    private RobotLineSeparatorsContexts extractSeperators(
            final ContextOutput contexts, LineTokenPosition lineBoundaries) {
        RobotLineSeparatorsContexts sep = new RobotLineSeparatorsContexts();
        for (IContextRecognizer sepRecognizer : separatorRecognizers) {
            sep.addNextSeparators(sepRecognizer.recognize(contexts,
                    lineBoundaries));
        }

        return sep;
    }

    public static class ContextOutput {

        private final List<AggregatedOneLineRobotContexts> contexts = new LinkedList<>();
        private final TokenOutput tokenOutput;


        public ContextOutput(final TokenOutput tokenOutput) {
            this.tokenOutput = tokenOutput;
        }


        public TokenOutput getTokenizedContent() {
            return tokenOutput;
        }


        public List<AggregatedOneLineRobotContexts> getContexts() {
            return contexts;
        }
    }
}
