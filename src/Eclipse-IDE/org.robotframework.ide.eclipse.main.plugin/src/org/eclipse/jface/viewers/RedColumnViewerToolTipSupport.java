package org.eclipse.jface.viewers;

import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

public class RedColumnViewerToolTipSupport extends ColumnViewerToolTipSupport {

    private RedColumnViewerToolTipSupport(final ColumnViewer viewer, final int style, final boolean manualActivation) {
        super(viewer, style, manualActivation);
    }

    public static void enableFor(final ColumnViewer viewer) {
        final RedColumnViewerToolTipSupport tooltipsSupport = new RedColumnViewerToolTipSupport(viewer, ToolTip.NO_RECREATE, false);
        tooltipsSupport.setHideOnMouseDown(false);
    }

    @Override
    protected Composite createViewerToolTipContentArea(final Event event, final ViewerCell cell, final Composite parent) {
        return super.createViewerToolTipContentArea(event, cell, parent);
    }
}
