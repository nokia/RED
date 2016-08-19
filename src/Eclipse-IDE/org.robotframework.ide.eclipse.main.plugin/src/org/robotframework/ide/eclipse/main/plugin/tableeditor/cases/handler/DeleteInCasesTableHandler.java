package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
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
            if (selectedElement instanceof RobotKeywordCall) {
                final RobotKeywordCall keywordCall = (RobotKeywordCall) selectedElement;
                if (columnIndex == 0
                        && keywordCall.getLinkedElement().getModelType() == ModelType.TEST_CASE_EXECUTABLE_ROW) {
                    return new SetCaseKeywordCallNameCommand(keywordCall, "");
                } else if (columnIndex > 0 && columnIndex < tableColumnCount - 1) {
                    return new SetCaseKeywordCallArgumentCommand(keywordCall, columnIndex - 1, null);
                } else if (columnIndex == tableColumnCount - 1) {
                    return new SetCaseKeywordCallCommentCommand(keywordCall, null);
                }
            } else if (selectedElement instanceof RobotCase && columnIndex == 0) {
                return new SetCaseNameCommand((RobotCase) selectedElement, "\\");
            }
            return null;
        }
    }
}
