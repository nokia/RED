package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.cmd.DeleteKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.DeleteKeywordDefinitionCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.DeleteKeywordsHandler.E4DeleteKeywordsHandler;
import org.robotframework.viewers.Selections;

public class DeleteKeywordsHandler extends DIHandler<E4DeleteKeywordsHandler> {

    public DeleteKeywordsHandler() {
        super(E4DeleteKeywordsHandler.class);
    }

    public static class E4DeleteKeywordsHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object deleteKeywords(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final List<RobotKeywordCall> keywordCalls = Selections.getElements(selection, RobotKeywordCall.class);
            final List<RobotKeywordDefinition> keywordDefinitions = Selections.getElements(selection, RobotKeywordDefinition.class);

            // it's not possible to have both lists non-empty (the handler is disabled in this situation)
            
            if (!keywordCalls.isEmpty()) {
                commandsStack.execute(new DeleteKeywordCallCommand(keywordCalls));
            } else if (!keywordDefinitions.isEmpty()) {
                commandsStack.execute(new DeleteKeywordDefinitionCommand(keywordDefinitions));
            }

            return null;
        }
    }
}
