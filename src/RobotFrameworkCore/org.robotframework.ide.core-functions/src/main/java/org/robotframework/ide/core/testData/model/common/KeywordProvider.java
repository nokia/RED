package org.robotframework.ide.core.testData.model.common;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class KeywordProvider extends AModelElement {

    public KeywordProvider(RobotLine containingLine, LineElement originalElement) {
        super(ElementType.LIBRARY_CONTAINS_KEYWORD, containingLine,
                originalElement);
    }
}
