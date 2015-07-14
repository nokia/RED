package org.robotframework.ide.core.testData.model.table.settings.resource;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class ImportedResource extends AModelElement {

    public ImportedResource(RobotLine containingLine,
            LineElement originalElement) {
        super(ElementType.IMPORT_RESOURCE, containingLine, originalElement);
    }
}
