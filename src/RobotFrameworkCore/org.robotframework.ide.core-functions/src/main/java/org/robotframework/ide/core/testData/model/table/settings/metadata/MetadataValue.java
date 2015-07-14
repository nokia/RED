package org.robotframework.ide.core.testData.model.table.settings.metadata;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class MetadataValue extends AModelElement {

    public MetadataValue(RobotLine containingLine, LineElement originalElement) {
        super(ElementType.METADATA_VALUE, containingLine, originalElement);
    }
}
