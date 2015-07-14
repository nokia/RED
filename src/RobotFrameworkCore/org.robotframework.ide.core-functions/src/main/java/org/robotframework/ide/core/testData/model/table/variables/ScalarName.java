package org.robotframework.ide.core.testData.model.table.variables;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class ScalarName extends AModelElement {

    public ScalarName(RobotLine containingLine, LineElement originalElement) {
        super(ElementType.VARIABLE_SCALAR, containingLine, originalElement);
    }
}
