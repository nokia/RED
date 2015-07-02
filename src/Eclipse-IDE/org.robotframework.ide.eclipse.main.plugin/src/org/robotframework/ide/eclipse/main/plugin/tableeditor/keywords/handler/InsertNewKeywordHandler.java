package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateFreshKeywordDefinitionCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.InsertNewKeywordHandler.E4InsertNewKeywordHandler;
import org.robotframework.viewers.Selections;

public class InsertNewKeywordHandler extends DIHandler<E4InsertNewKeywordHandler> {

    public InsertNewKeywordHandler() {
        super(E4InsertNewKeywordHandler.class);
    }

    public static class E4InsertNewKeywordHandler {
        @Inject
        private RobotEditorCommandsStack stack;

        @Execute
        public Object addNewUserDefinedKeyword(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final RobotElement selectedElement = Selections.getSingleElement(selection, RobotElement.class);

            RobotSuiteFileSection section = null;
            RobotKeywordDefinition definition = null;
            if (selectedElement instanceof RobotKeywordCall) {
                definition = (RobotKeywordDefinition) selectedElement.getParent();
                section = ((RobotKeywordCall) selectedElement).getSection();
            } else if (selectedElement instanceof RobotKeywordDefinition) {
                definition = (RobotKeywordDefinition) selectedElement;
                section = (RobotSuiteFileSection) definition.getParent();
            }

            if (section == null || definition == null) {
                return null;
            }

            final int index = section.getChildren().indexOf(definition);
            stack.execute(new CreateFreshKeywordDefinitionCommand((RobotKeywordsSection) section, index));
            return null;
        }
    }
}
