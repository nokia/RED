package org.robotframework.ide.core.testData.model.common;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class CommentDeclaration extends AModelElement {

    public CommentDeclaration(ElementType type, RobotLine containingLine,
            LineElement originalElement) {
        super(ElementType.DECLARED_COMMENT, containingLine, originalElement);
    }
}
