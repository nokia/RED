package org.robotframework.ide.core.testData.model.table.settings.test;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class TestTemplateDeclaration extends AModelElement {

    public TestTemplateDeclaration(RobotLine containingLine,
            LineElement originalElement) {
        super(ElementType.SETTINGS_TEST_TEMPLATE, containingLine,
                originalElement);
    }
}
