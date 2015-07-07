package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetKeywordDefinitionArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetKeywordDefinitionCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.SetKeywordDefinitionNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.DeleteCellContentHandler.E4DeleteCellContentHandler;
import org.robotframework.viewers.Selections;

public class DeleteCellContentHandler extends DIHandler<E4DeleteCellContentHandler> {

    public DeleteCellContentHandler() {
        super(E4DeleteCellContentHandler.class);
    }

    public static class E4DeleteCellContentHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object deleteContent(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final FocusedViewerAccessor viewerAccessor) {
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
