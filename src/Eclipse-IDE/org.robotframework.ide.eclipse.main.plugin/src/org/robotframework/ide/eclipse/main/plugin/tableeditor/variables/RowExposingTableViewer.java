package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

public class RowExposingTableViewer extends TableViewer {

    public RowExposingTableViewer(final Composite parent, final int style) {
        super(parent, style);
    }

    @Override
    public ViewerRow getViewerRowFromItem(final Widget item) { // changing protected to public
        return super.getViewerRowFromItem(item);
    }
}