package org.robotframework.ide.core.testData.model.table.settings.tags;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class ForceTagsDeclaration extends AModelElement {

    public ForceTagsDeclaration(RobotLine containingLine,
            LineElement originalElement) {
        super(ElementType.SETTINGS_FORCE_TAGS, containingLine, originalElement);
    }
}
