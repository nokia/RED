package org.robotframework.ide.core.testData.model.table.testCases.settings;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class PostconditionDeclaration extends AModelElement {

    public PostconditionDeclaration(RobotLine containingLine,
            LineElement originalElement) {
        super(ElementType.TEST_CASE_POSTCONDITION, containingLine,
                originalElement);
    }
}
