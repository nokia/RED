package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.cmd.DeleteCasesCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.DeleteKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.DeleteCasesHandler.E4DeleteCasesHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteCasesHandler extends DIHandler<E4DeleteCasesHandler> {

    public DeleteCasesHandler() {
        super(E4DeleteCasesHandler.class);
    }

    public static class E4DeleteCasesHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object deleteCases(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final List<RobotKeywordCall> keywordCalls = Selections.getElements(selection, RobotKeywordCall.class);
            final List<RobotCase> cases = Selections.getElements(selection, RobotCase.class);

            // it's not possible to have both lists non-empty (the handler is
            // disabled in this situation)

            if (!keywordCalls.isEmpty()) {
                commandsStack.execute(new DeleteKeywordCallCommand(keywordCalls));
            } else if (!cases.isEmpty()) {
                commandsStack.execute(new DeleteCasesCommand(cases));
            }

            return null;
        }
    }
}
