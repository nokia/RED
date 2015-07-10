package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.CutCellContentHandler.E4CutCellContentHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.DeleteCellContentHandler.E4DeleteCellContentHandler;
import org.robotframework.viewers.Selections;

public class CutCellContentHandler extends DIHandler<E4CutCellContentHandler> {

    public CutCellContentHandler() {
        super(E4CutCellContentHandler.class);
    }

    public static class E4CutCellContentHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object cutContent(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final FocusedViewerAccessor viewerAccessor, final Clipboard clipboard) {

            final ViewerCell focusedCell = viewerAccessor.getFocusedCell();
            final String cellContent = focusedCell.getText();
            clipboard.setContents(new String[] { cellContent }, new Transfer[] { TextTransfer.getInstance() });

            final RobotElement element = Selections.getSingleElement(selection, RobotElement.class);
            final int index = viewerAccessor.getFocusedCell().getColumnIndex();
            final int noOfColumns = ((TreeViewer) viewerAccessor.getViewer()).getTree().getColumnCount();
            
            E4DeleteCellContentHandler.setAttributeAtPosition(commandsStack, element, index, noOfColumns, "");
            
            return null;
        }
    }
}
