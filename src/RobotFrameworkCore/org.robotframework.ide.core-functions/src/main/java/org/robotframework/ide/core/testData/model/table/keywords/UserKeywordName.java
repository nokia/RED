package org.robotframework.ide.core.testData.model.table.keywords;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class UserKeywordName extends AModelElement {

    public UserKeywordName(RobotLine containingLine, LineElement originalElement) {
        super(ElementType.USER_KEYWORD_NAME, containingLine, originalElement);
    }
}
