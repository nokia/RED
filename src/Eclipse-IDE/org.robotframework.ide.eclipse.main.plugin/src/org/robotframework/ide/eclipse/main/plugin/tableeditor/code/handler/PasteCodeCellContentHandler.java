package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.PasteCodeCellContentHandler.E4PasteCodeCellContentHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4PasteCellContentHandler;

import com.google.common.base.Optional;

public class PasteCodeCellContentHandler extends DIHandler<E4PasteCodeCellContentHandler> {

    public PasteCodeCellContentHandler() {
        super(E4PasteCodeCellContentHandler.class);
    }

    public static class E4PasteCodeCellContentHandler extends E4PasteCellContentHandler {
        @Override
        protected Optional<? extends EditorCommand> provideCommandForAttributeChange(final RobotElement element,
                final int index, final int noOfColumns, final String newAttribute) {
            return new CodeAttributesCommandsProvider().provideChangeAttributeCommand(element, index, noOfColumns,
                    newAttribute);
        }
    }
}
