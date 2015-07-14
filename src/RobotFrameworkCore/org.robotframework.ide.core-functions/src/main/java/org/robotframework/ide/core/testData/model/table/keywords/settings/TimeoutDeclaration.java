package org.robotframework.ide.core.testData.model.table.keywords.settings;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class TimeoutDeclaration extends AModelElement {

    public TimeoutDeclaration(RobotLine containingLine,
            LineElement originalElement) {
        super(ElementType.KEYWORD_TIMEOUT, containingLine, originalElement);
    }
}
