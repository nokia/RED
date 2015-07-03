package org.robotframework.ide.core.testData.text.context;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.recognizer.DeclaredCommentRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.DoubleSpaceOrTabulatorSeparatorRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.IContextRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.PipeSeparatorRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.SettingTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.TestCaseTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.context.recognizer.VariableTableHeaderRecognizer;
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
 * @see SettingTableHeaderRecognizer
 * @see VariableTableHeaderRecognizer
 * @see TestCaseTableHeaderRecognizer
 * @see DoubleSpaceOrTabulatorSeparatorRecognizer
 * @see PipeSeparatorRecognizer
 */
public class ContextBuilder {

    public ContextOutput buildContexts(final TokenOutput tokenOutput) {
        ContextOutput out = new ContextOutput(tokenOutput);
        TokensLineIterator lineIter = new TokensLineIterator(tokenOutput);

        return out;
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
