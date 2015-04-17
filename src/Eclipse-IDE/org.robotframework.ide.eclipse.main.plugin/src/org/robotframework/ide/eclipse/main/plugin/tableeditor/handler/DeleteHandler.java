package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.cmd.DeleteVariableCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.DeleteHandler.E4DeleteHandler;
import org.robotframework.viewers.Selections;

public class DeleteHandler extends DIHandler<E4DeleteHandler> {

    public DeleteHandler() {
        super(E4DeleteHandler.class);
    }

    public static class E4DeleteHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object deleteVariables(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final List<RobotVariable> variables = Selections.getElements(selection, RobotVariable.class);
            commandsStack.execute(new DeleteVariableCommand(variables));

            return null;
        }
    }
}
