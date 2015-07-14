package org.robotframework.ide.core.testData.model.common;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class ArgumentValue extends AModelElement {

    public ArgumentValue(RobotLine containingLine, LineElement originalElement) {
        super(ElementType.ARGUMENT_PARAMETER_VALUE, containingLine,
                originalElement);
    }
}
