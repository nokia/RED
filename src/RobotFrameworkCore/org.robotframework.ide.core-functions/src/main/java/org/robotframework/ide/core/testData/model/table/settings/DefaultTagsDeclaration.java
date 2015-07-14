package org.robotframework.ide.core.testData.model.table.settings;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class DefaultTagsDeclaration extends AModelElement {

    public DefaultTagsDeclaration(RobotLine containingLine,
            LineElement originalElement) {
        super(ElementType.SETTINGS_DEFAULT_TAGS, containingLine,
                originalElement);
    }
}
