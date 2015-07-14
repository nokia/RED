package org.robotframework.ide.core.testData.model.table.settings.test;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class TestTimeoutDeclaration extends AModelElement {

    public TestTimeoutDeclaration(RobotLine containingLine,
            LineElement originalElement) {
        super(ElementType.SETTINGS_TEST_TIMEOUT, containingLine,
                originalElement);
    }
}
