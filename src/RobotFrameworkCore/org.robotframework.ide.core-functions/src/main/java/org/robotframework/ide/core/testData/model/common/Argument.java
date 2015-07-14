package org.robotframework.ide.core.testData.model.common;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class Argument extends AModelElement {

    public ArgumentName name;
    public ArgumentValue value;


    public Argument(RobotLine containingLine, LineElement originalElement) {
        super(ElementType.IMPORT_LIBRARY_INITAL_ARGUMENT, containingLine,
                originalElement);
    }
}
