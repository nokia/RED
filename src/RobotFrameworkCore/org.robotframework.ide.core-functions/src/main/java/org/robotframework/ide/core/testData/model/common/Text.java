package org.robotframework.ide.core.testData.model.common;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class Text extends AModelElement {

    public Text(RobotLine containingLine,
            LineElement originalElement) {
        super(ElementType.VALUE, containingLine, originalElement);
    }
}
