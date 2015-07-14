package org.robotframework.ide.core.testData.model.table.settings.doc;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class DocumentationDeclaration extends AModelElement {

    public DocumentationDeclaration(RobotLine containingLine,
            LineElement originalElement) {
        super(ElementType.SETTINGS_DOCUMENTATION, containingLine,
                originalElement);
    }
}
