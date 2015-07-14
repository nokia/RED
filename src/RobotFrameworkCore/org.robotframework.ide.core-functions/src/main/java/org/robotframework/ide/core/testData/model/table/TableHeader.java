package org.robotframework.ide.core.testData.model.table;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.RobotLine;


public class TableHeader extends AModelElement {

    public TableHeader(RobotLine containingLine,
            LineElement originalElement) {
        super(containingLine, originalElement);
    }
}
