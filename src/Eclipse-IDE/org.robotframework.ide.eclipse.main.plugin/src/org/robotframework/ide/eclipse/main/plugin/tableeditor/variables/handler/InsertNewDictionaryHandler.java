package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable.Type;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshVariableCommand;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.InsertNewDictionaryHandler.E4InsertNewHandler;
import org.robotframework.red.viewers.Selections;

public class InsertNewDictionaryHandler extends DIHandler<E4InsertNewHandler> {

    public InsertNewDictionaryHandler() {
        super(E4InsertNewHandler.class);
    }

    public static class E4InsertNewHandler {

        @Inject
        private RobotEditorCommandsStack stack;

        @Execute
        public Object addNewVariable(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final RobotVariable selectedVariable = Selections.getSingleElement(selection, RobotVariable.class);
            final RobotVariablesSection variablesSection = selectedVariable.getParent();
            final int index = variablesSection.getChildren().indexOf(selectedVariable);
            
            stack.execute(new CreateFreshVariableCommand(variablesSection, index, Type.DICTIONARY));
            return null;
        }
    }
}
