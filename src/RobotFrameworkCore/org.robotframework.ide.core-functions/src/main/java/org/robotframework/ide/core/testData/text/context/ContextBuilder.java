package org.robotframework.ide.core.testData.text.context;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.context.recognizer.DeclaredCommentContext;
import org.robotframework.ide.core.testData.text.context.recognizer.IContextRecognizer;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;

import com.google.common.collect.LinkedListMultimap;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 */
public class ContextBuilder {

    private List<IContextRecognizer> recognizers = new LinkedList<>();


    public ContextBuilder() {
        recognizers.add(new DeclaredCommentContext());
        recognizers = Collections.unmodifiableList(recognizers);
    }


    public ContextOutput buildContexts(final TokenOutput tokenOutput) {
        ContextOutput out = new ContextOutput(tokenOutput);

        TokensLineIterator lineIter = new TokensLineIterator(tokenOutput);
        while(lineIter.hasNext()) {
            LineTokenPosition lineInterval = lineIter.next();

            LinkedListMultimap<IContextRecognizer, IContextElement> foundPerLine = LinkedListMultimap
                    .create();
            for (IContextRecognizer recognizer : recognizers) {
                List<IContextElement> foundContexts = recognizer.recognize(out,
                        lineInterval);
                foundPerLine.putAll(recognizer, foundContexts);
            }
        }

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
