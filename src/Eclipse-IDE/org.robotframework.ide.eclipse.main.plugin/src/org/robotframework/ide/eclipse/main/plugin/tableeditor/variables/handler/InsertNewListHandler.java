package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateFreshVariableCommand;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable.Type;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.InsertNewListHandler.E4InsertNewHandler;
import org.robotframework.red.viewers.Selections;

public class InsertNewListHandler extends DIHandler<E4InsertNewHandler> {

    public InsertNewListHandler() {
        super(E4InsertNewHandler.class);
    }

    public static class E4InsertNewHandler {

        @Inject
        private RobotEditorCommandsStack stack;

        @Execute
        public Object addNewVariable(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final RobotVariable selectedVariable = Selections.getSingleElement(selection, RobotVariable.class);
            final RobotSuiteFileSection variablesSection = (RobotSuiteFileSection) selectedVariable.getParent();
            final int index = variablesSection.getChildren().indexOf(selectedVariable);
            
            stack.execute(new CreateFreshVariableCommand(variablesSection, index, Type.LIST));
            return null;
        }
    }
}
