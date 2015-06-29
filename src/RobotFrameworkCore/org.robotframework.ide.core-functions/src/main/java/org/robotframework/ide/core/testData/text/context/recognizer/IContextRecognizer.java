package org.robotframework.ide.core.testData.text.context.recognizer;

import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 */
public interface IContextRecognizer {

    List<IContextElement> recognize(final ContextOutput currentContext,
            final LineTokenPosition lineInterval);
}
