package org.robotframework.ide.core.testData.model.table.settings.test;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class TestTeardownDeclaration extends AModelElement {

    public TestTeardownDeclaration(RobotLine containingLine,
            LineElement originalElement) {
        super(ElementType.SETTINGS_SUITE_TEARDOWN, containingLine,
                originalElement);
    }
}
