package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.CutCodeCellContentHandler.E4CutCodeCellContentHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4CutCellContentHandler;

import com.google.common.base.Optional;

public class CutCodeCellContentHandler extends DIHandler<E4CutCodeCellContentHandler> {

    public CutCodeCellContentHandler() {
        super(E4CutCodeCellContentHandler.class);
    }

    public static class E4CutCodeCellContentHandler extends E4CutCellContentHandler {
        @Override
        protected Optional<? extends EditorCommand> provideCommandForAttributeChange(final RobotElement element,
                final int index, final int noOfColumns) {
            return new CodeAttributesCommandsProvider().provide(element, index, noOfColumns, "");
        }
    }
}
