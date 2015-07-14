package org.robotframework.ide.core.testData.model.table.testCases.settings;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class DocumentationDeclaration extends AModelElement {

    public DocumentationDeclaration(RobotLine containingLine,
            LineElement originalElement) {
        super(ElementType.TEST_CASE_DOCUMENTATION, containingLine,
                originalElement);
    }
}
