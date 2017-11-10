package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesTableValuesChangingCommandsCollector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.DeleteInCasesTableHandler.E4DeleteInCasesTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4DeleteInTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class DeleteInCasesTableHandler extends DIParameterizedHandler<E4DeleteInCasesTableHandler> {

    public DeleteInCasesTableHandler() {
        super(E4DeleteInCasesTableHandler.class);
    }

    public static class E4DeleteInCasesTableHandler extends E4DeleteInTableHandler {

        @Override
        protected EditorCommand getCommandForSelectedElement(final RobotElement selectedElement, final int columnIndex,
                final int tableColumnCount) {
            final List<? extends EditorCommand> commands = new CasesTableValuesChangingCommandsCollector()
                    .collectForRemoval(selectedElement, columnIndex);
            return commands.isEmpty() ? null : commands.get(0);
        }
    }
}
