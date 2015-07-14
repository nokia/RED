package org.robotframework.ide.core.testData.model.table.testCases.settings;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class TagsDeclaration extends AModelElement {

    public TagsDeclaration(RobotLine containingLine, LineElement originalElement) {
        super(ElementType.TEST_CASE_TAGS, containingLine, originalElement);
    }
}
