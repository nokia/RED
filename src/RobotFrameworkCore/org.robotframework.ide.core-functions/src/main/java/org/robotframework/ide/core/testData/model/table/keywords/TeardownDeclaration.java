package org.robotframework.ide.core.testData.model.table.keywords;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class TeardownDeclaration extends AModelElement {

    public TeardownDeclaration(RobotLine containingLine,
            LineElement originalElement) {
        super(ElementType.KEYWORD_TEARDOWN, containingLine, originalElement);
    }
}
