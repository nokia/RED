package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.DeleteCellHandler.E4DeleteCellHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteCellHandler extends DIHandler<E4DeleteCellHandler> {

    public DeleteCellHandler() {
        super(E4DeleteCellHandler.class);
    }

    public static class E4DeleteCellHandler {

        @Execute
        public Object deleteCell(final RobotEditorCommandsStack commandsStack,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {
            final FocusedViewerAccessor viewerAccessor = editor.getFocusedViewerAccessor();
            final RobotCase testCase = Selections.getOptionalFirstElement(selection, RobotCase.class).orNull();
            final RobotKeywordCall call = Selections.getOptionalFirstElement(selection, RobotKeywordCall.class)
                    .orNull();

            System.err.println(viewerAccessor.getFocusedCell().getColumnIndex());
            if (testCase != null) {
                // commandsStack.execute(new DeleteCasesCommand(cases));
            } else if (call != null) {
                // commandsStack.execute(new DeleteKeywordCallCommand(calls));
            }
            return null;
        }
    }
}
