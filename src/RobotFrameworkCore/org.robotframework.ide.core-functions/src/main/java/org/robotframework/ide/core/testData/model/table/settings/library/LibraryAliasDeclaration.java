package org.robotframework.ide.core.testData.model.table.settings.library;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class LibraryAliasDeclaration extends AModelElement {

    public LibraryAliasDeclaration(RobotLine containingLine, LineElement originalElement) {
        super(ElementType.IMPORT_LIBRARY_ALIASES, containingLine,
                originalElement);
    }
}
