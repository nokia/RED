package org.robotframework.ide.core.testData.model.common;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class KeywordUsage extends AModelElement {

    public KeywordUsage(RobotLine containingLine, LineElement originalElement) {
        super(ElementType.KEYWORD_NAME, containingLine, originalElement);
    }
}
