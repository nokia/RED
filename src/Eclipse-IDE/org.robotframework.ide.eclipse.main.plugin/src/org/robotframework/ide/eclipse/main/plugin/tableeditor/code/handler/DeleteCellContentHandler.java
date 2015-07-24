package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetCaseCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetCaseNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.DeleteCellContentHandler.E4DeleteCellContentHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteCellContentHandler extends DIHandler<E4DeleteCellContentHandler> {

    public DeleteCellContentHandler() {
        super(E4DeleteCellContentHandler.class);
    }

    public static class E4DeleteCellContentHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object deleteContent(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor) {
            final FocusedViewerAccessor viewerAccessor = editor.getFocusedViewerAccessor();
            final RobotElement element = Selections.getSingleElement(selection, RobotElement.class);
            final int index = viewerAccessor.getFocusedCell().getColumnIndex();
            final int noOfColumns = ((TreeViewer) viewerAccessor.getViewer()).getTree().getColumnCount();

            E4DeleteCellContentHandler.setAttributeAtPosition(commandsStack, element, index, noOfColumns, "");

            return null;
        }

        public static void setAttributeAtPosition(final RobotEditorCommandsStack commandsStack,
                final RobotElement element, final int index, final int noOfColumns, final String attribute) {
            if (element instanceof RobotKeywordDefinition) {
                final RobotKeywordDefinition definition = (RobotKeywordDefinition) element;
                if (index == 0) {
                    commandsStack.execute(new SetKeywordDefinitionNameCommand(definition, attribute));
                } else if (index == noOfColumns - 1) {
                    commandsStack.execute(new SetKeywordDefinitionCommentCommand(definition, attribute));
                } else {
                    commandsStack.execute(new SetKeywordDefinitionArgumentCommand(definition, index - 1, attribute));
                }
            } else if (element instanceof RobotCase) {
                final RobotCase testCase = (RobotCase) element;
                if (index == 0) {
                    commandsStack.execute(new SetCaseNameCommand(testCase, attribute));
                } else if (index == noOfColumns - 1) {
                    commandsStack.execute(new SetCaseCommentCommand(testCase, attribute));
                }
            } else if (element instanceof RobotKeywordCall) {
                final RobotKeywordCall call = (RobotKeywordCall) element;
                if (index == 0) {
                    commandsStack.execute(new SetKeywordCallNameCommand(call, attribute));
                } else if (index == noOfColumns - 1) {
                    commandsStack.execute(new SetKeywordCallCommentCommand(call, attribute));
                } else {
                    commandsStack.execute(new SetKeywordCallArgumentCommand(call, index - 1, attribute));
                }
            }
        }
    }
}
