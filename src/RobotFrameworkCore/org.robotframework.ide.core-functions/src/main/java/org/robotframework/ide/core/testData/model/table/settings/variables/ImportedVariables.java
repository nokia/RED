package org.robotframework.ide.core.testData.model.table.settings.variables;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class ImportedVariables extends AModelElement {

    public ImportedVariables(RobotLine containingLine,
            LineElement originalElement) {
        super(ElementType.IMPORT_VARIABLES, containingLine, originalElement);
    }
}
