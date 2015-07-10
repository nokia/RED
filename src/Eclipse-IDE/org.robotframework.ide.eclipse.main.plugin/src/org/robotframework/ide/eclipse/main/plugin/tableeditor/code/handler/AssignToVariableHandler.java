package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.AssignToVariableHandler.E4AssignToVariableHandler;
import org.robotframework.viewers.Selections;

public class AssignToVariableHandler extends DIHandler<E4AssignToVariableHandler> {

    public AssignToVariableHandler() {
        super(E4AssignToVariableHandler.class);
    }

    public static class E4AssignToVariableHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object assignToVariable(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            throw new RuntimeException("Not yet implemented!");
        }
    }
}
