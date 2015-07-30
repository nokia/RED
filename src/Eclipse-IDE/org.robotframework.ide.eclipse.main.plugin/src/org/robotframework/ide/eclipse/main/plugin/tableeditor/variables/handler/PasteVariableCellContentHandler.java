package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4PasteCellContentHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.PasteVariableCellContentHandler.E4PasteVariableCellContentHandler;

import com.google.common.base.Optional;

public class PasteVariableCellContentHandler extends DIHandler<E4PasteVariableCellContentHandler> {

    public PasteVariableCellContentHandler() {
        super(E4PasteVariableCellContentHandler.class);
    }

    public static class E4PasteVariableCellContentHandler extends E4PasteCellContentHandler {
        @Override
        protected Optional<? extends EditorCommand> provideCommandForAttributeChange(final RobotElement element,
                final int index, final int noOfColumns, final String newAttribute) {
            return new VariablesAttributesCommandsProvider().provide(element, index, newAttribute);
        }
    }
}
