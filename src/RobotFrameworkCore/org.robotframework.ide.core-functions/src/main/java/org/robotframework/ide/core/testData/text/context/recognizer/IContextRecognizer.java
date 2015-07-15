package org.robotframework.ide.core.testData.text.context.recognizer;

import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator.LineTokenPosition;


/**
 * Extract functionality need for recognize if in given line expected tokens are
 * present.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 */
public interface IContextRecognizer {

    /**
     * 
     * @param currentContext
     *            current context builded
     * @param lineInterval
     *            boundaries for line in token list
     * @return
     */
    List<IContextElement> recognize(final ContextOutput currentContext,
            final LineTokenPosition lineInterval);


    /**
     * 
     * @return main type of context build up by this recognizer, it could be
     *         situation where one recognizer are build few contexts in example
     *         escaped variable
     */
    IContextElementType getContextType();
}
