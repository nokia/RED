package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.MoveVariableDownCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.MoveVariableDownHandler.E4MoveVariableDownHandler;
import org.robotframework.red.viewers.Selections;

public class MoveVariableDownHandler extends DIHandler<E4MoveVariableDownHandler> {

    public MoveVariableDownHandler() {
        super(E4MoveVariableDownHandler.class);
    }

    public static class E4MoveVariableDownHandler {

        @Inject
        private RobotEditorCommandsStack stack;

        @Execute
        public Object moveVariableDown(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final RobotVariable selectedVariable = Selections.getSingleElement(selection, RobotVariable.class);
            final RobotVariablesSection variablesSection = selectedVariable.getParent();
            final int index = variablesSection.getChildren().indexOf(selectedVariable);

            stack.execute(new MoveVariableDownCommand(variablesSection, index));
            return null;
        }
    }
}
