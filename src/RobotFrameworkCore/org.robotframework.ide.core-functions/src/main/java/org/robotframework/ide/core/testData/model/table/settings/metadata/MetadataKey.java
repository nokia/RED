package org.robotframework.ide.core.testData.model.table.settings.metadata;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class MetadataKey extends AModelElement {

    public MetadataKey(RobotLine containingLine, LineElement originalElement) {
        super(ElementType.METADATA_KEY, containingLine, originalElement);
    }
}
