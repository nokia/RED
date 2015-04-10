package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.robotframework.ide.eclipse.main.plugin.RobotTheme;

class VariableCellsHighlighter extends FocusCellOwnerDrawHighlighter {

    VariableCellsHighlighter(final ColumnViewer viewer) {
        super(viewer);
    }

    @Override
    protected boolean onlyTextHighlighting(final ViewerCell cell) {
        return false;
    }

    @Override
    protected Color getSelectedCellBackgroundColor(final ViewerCell cell) {
        return RobotTheme.getHighlightedCellColor();
    }

    @Override
    protected Color getSelectedCellBackgroundColorNoFocus(final ViewerCell cell) {
        return cell.getItem().getDisplay().getSystemColor(SWT.COLOR_GRAY);
    }

    @Override
    protected Color getSelectedCellForegroundColor(final ViewerCell cell) {
        return cell.getItem().getDisplay().getSystemColor(SWT.COLOR_BLACK);
    }
}
