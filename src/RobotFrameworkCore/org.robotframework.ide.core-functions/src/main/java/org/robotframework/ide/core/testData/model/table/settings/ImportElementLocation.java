package org.robotframework.ide.core.testData.model.table.settings;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class ImportElementLocation extends AModelElement {

    public ImportElementLocation(RobotLine containingLine,
            LineElement originalElement) {
        super(ElementType.IMPORT_LOCATION, containingLine, originalElement);
    }
}
