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
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.EscapedAtSign;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.EscapedBackslashSign;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.EscapedDollarSign;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.EscapedEqualsSign;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.EscapedHashSign;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.EscapedPipeSign;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.EscapedProcentSign;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.EscapedSpace;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.LineFeedTextualRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.PipeSeparatorRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.TabulatorTextualRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.UnicodeCharacterWithHexValue;
import org.robotframework.ide.core.testData.text.context.recognizer.keywordTable.KeywordArgumentsDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.keywordTable.KeywordDocumentationDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.keywordTable.KeywordReturnDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.keywordTable.KeywordTeardownDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.keywordTable.KeywordTimeoutDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.DefaultTagsDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.DocumentationDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.ForceTagsDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.ImportLibraryAliasDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.ImportLibraryDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.ImportResourceDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.ImportVariablesDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.MetadataDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.SuitePostconditionDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.SuitePreconditionDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.SuiteSetupDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.SuiteTeardownDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.TestPostconditionDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.TestPreconditionDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.TestSetupDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.TestTeardownDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.TestTemplateDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.TestTimeoutDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable.TestDocumentationDeclaration;
import org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable.TestTagsDeclaration;
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
 * @see CollectionIndexPosition
 * 
 * @see EscapedDollarSign
 * @see EscapedAtSign
 * @see EscapedProcentSign
 * @see EscapedHashSign
 * @see EscapedPipeSign
 * @see EscapedBackslashSign
 * @see EscapedSpace
 * 
 * @see ImportLibraryDeclaration
 * @see ImportLibraryAliasDeclaration
 * @see ImportResourceDeclaration
 * @see ImportVariablesDeclaration
 * @see DocumentationDeclaration
 * @see MetadataDeclaration
 * @see SuiteSetupDeclaration
 * @see SuiteTeardownDeclaration
 * @see SuitePreconditionDeclaration
 * @see SuitePostconditionDeclaration
 * @see ForceTagsDeclaration
 * @see DefaultTagsDeclaration
 * @see TestSetupDeclaration
 * @see TestTeardownDeclaration
 * @see TestPreconditionDeclaration
 * @see TestPostconditionDeclaration
 * @see TestTemplateDeclaration
 * @see TestTimeoutDeclaration
 * 
 * @see TestDocumentationDeclaration
 * @see TestTagsDeclaration
 * @see TestSetupDeclaration
 * @see TestSetupDeclaration
 * @see TestTeardownDeclaration
 * @see TestPreconditionDeclaration
 * @see TestPostconditionDeclaration
 * @see TestTemplateDeclaration
 * @see TestTimeoutDeclaration
 * 
 * @see KeywordDocumentationDeclaration
 * @see KeywordArgumentsDeclaration
 * @see KeywordReturnDeclaration
 * @see KeywordTeardownDeclaration
 * @see KeywordTimeoutDeclaration
 * 
 * @see DoubleSpaceOrTabulatorSeparatorRecognizer
 * @see PipeSeparatorRecognizer
 * 
 */
public class ContextBuilder {

    private List<IContextRecognizer> separatorRecognizers = new LinkedList<>();
    private List<IContextRecognizer> normalRecognizers = new LinkedList<>();
    private List<IContextRecognizer> settingTableRecognizers = new LinkedList<>();
    private List<IContextRecognizer> testCaseTableRecognizers = new LinkedList<>();
    private List<IContextRecognizer> keywordTableRecognizers = new LinkedList<>();


    public ContextBuilder() {
        separatorRecognizers
                .add(new DoubleSpaceOrTabulatorSeparatorRecognizer());
        separatorRecognizers.add(new PipeSeparatorRecognizer());
        separatorRecognizers = Collections
                .unmodifiableList(separatorRecognizers);

        initIndependentFromContextRecognizers();
        initSettingTableRecognizers();
        initTestCaseTableRecognizers();
        initKeywordTableRecognizers();
    }


    protected void initKeywordTableRecognizers() {
        keywordTableRecognizers.add(new KeywordDocumentationDeclaration());
        keywordTableRecognizers.add(new KeywordArgumentsDeclaration());
        keywordTableRecognizers.add(new KeywordReturnDeclaration());
        keywordTableRecognizers.add(new KeywordTeardownDeclaration());
        keywordTableRecognizers.add(new KeywordTimeoutDeclaration());
        keywordTableRecognizers = Collections
                .unmodifiableList(keywordTableRecognizers);
    }


    protected void initTestCaseTableRecognizers() {
        testCaseTableRecognizers.add(new TestDocumentationDeclaration());
        testCaseTableRecognizers.add(new TestTagsDeclaration());
        testCaseTableRecognizers.add(new TestSetupDeclaration());
        testCaseTableRecognizers.add(new TestTeardownDeclaration());
        testCaseTableRecognizers.add(new TestPreconditionDeclaration());
        testCaseTableRecognizers.add(new TestPostconditionDeclaration());
        testCaseTableRecognizers.add(new TestTemplateDeclaration());
        testCaseTableRecognizers.add(new TestTimeoutDeclaration());
        testCaseTableRecognizers = Collections
                .unmodifiableList(testCaseTableRecognizers);
    }


    protected void initSettingTableRecognizers() {
        settingTableRecognizers.add(new ImportLibraryDeclaration());
        settingTableRecognizers.add(new ImportLibraryAliasDeclaration());
        settingTableRecognizers.add(new ImportResourceDeclaration());
        settingTableRecognizers.add(new ImportVariablesDeclaration());
        settingTableRecognizers.add(new DocumentationDeclaration());
        settingTableRecognizers.add(new MetadataDeclaration());
        settingTableRecognizers.add(new SuiteSetupDeclaration());
        settingTableRecognizers.add(new SuiteTeardownDeclaration());
        settingTableRecognizers.add(new SuitePreconditionDeclaration());
        settingTableRecognizers.add(new SuitePostconditionDeclaration());
        settingTableRecognizers.add(new ForceTagsDeclaration());
        settingTableRecognizers.add(new DefaultTagsDeclaration());
        settingTableRecognizers.add(new TestSetupDeclaration());
        settingTableRecognizers.add(new TestTeardownDeclaration());
        settingTableRecognizers.add(new TestPreconditionDeclaration());
        settingTableRecognizers.add(new TestPostconditionDeclaration());
        settingTableRecognizers.add(new TestTemplateDeclaration());
        settingTableRecognizers.add(new TestTimeoutDeclaration());
        settingTableRecognizers = Collections
                .unmodifiableList(settingTableRecognizers);
    }


    protected void initIndependentFromContextRecognizers() {
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

        normalRecognizers.add(new EscapedDollarSign());
        normalRecognizers.add(new EscapedAtSign());
        normalRecognizers.add(new EscapedProcentSign());
        normalRecognizers.add(new EscapedHashSign());
        normalRecognizers.add(new EscapedEqualsSign());
        normalRecognizers.add(new EscapedPipeSign());
        normalRecognizers.add(new EscapedBackslashSign());
        normalRecognizers.add(new EscapedSpace());

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

            performRecognizationOfElementsInLine(out, lineBoundaries, ctx);
        }

        return out;
    }


    private void performRecognizationOfElementsInLine(ContextOutput out,
            LineTokenPosition lineBoundaries, AggregatedOneLineRobotContexts ctx) {
        for (IContextRecognizer recognizer : normalRecognizers) {
            List<IContextElement> recognize = recognizer.recognize(out,
                    lineBoundaries);
            addFoundContexts(ctx, recognize);
        }

        for (IContextRecognizer recognizer : settingTableRecognizers) {
            List<IContextElement> recognize = recognizer.recognize(out,
                    lineBoundaries);
            addFoundContexts(ctx, recognize);
        }

        for (IContextRecognizer recognizer : testCaseTableRecognizers) {
            List<IContextElement> recognize = recognizer.recognize(out,
                    lineBoundaries);
            addFoundContexts(ctx, recognize);
        }

        for (IContextRecognizer recognizer : keywordTableRecognizers) {
            List<IContextElement> recognize = recognizer.recognize(out,
                    lineBoundaries);
            addFoundContexts(ctx, recognize);
        }
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
