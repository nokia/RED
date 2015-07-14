package org.robotframework.ide.core.testData.model.common;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class Argument extends AModelElement {

    public ArgumentName name;
    public OptionalEquals equals;
    public ArgumentValue value;


    public Argument(RobotLine containingLine, LineElement originalElement) {
        super(ElementType.ARGUMENT, containingLine, originalElement);
    }

    public static class OptionalEquals extends AModelElement {

        public OptionalEquals(RobotLine containgLine,
                LineElement originalElement) {
            super(ElementType.ARGUMENT_EQUALS, containgLine, originalElement);
        }
    }
}
