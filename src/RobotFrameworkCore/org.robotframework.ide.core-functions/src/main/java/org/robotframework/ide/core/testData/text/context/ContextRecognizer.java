package org.robotframework.ide.core.testData.text.context;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 */
public class ContextRecognizer {

    public ContextOutput buildContexts(final TokenOutput tokenOutput) {
        ContextOutput out = new ContextOutput(tokenOutput);

        return out;
    }

    public static class ContextOutput {

        private final TokenOutput tokenOutput;
        private List<IContextElement> contexts = new LinkedList<>();


        public ContextOutput(final TokenOutput tokenOutput) {
            this.tokenOutput = tokenOutput;
        }


        public TokenOutput getTokenizedContent() {
            return tokenOutput;
        }


        public List<IContextElement> getContexts() {
            return contexts;
        }
    }
}
