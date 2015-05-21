package org.eclipse.jface.viewers;

import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Table;

/**
 * Helper methods for common configuration operations on
 * {@link org.eclipse.jface.viewers.TableViewer} and
 * {@link org.eclipse.jface.viewers.TreeViewer}
 * 
 * @{author Michal Anglart
 */
public class ViewerControlConfigurator {

    /**
     * Disables context menu if the header of Table is clicked. Under Windows
     * normally context menu is also shown when clicking on table header
     * 
     * @param table
     *            Table for which header context menu should be disabled
     */
    public static void disableContextMenuOnHeader(final Table table) {
        // no need to dispose
        table.addMenuDetectListener(new MenuDetectListener() {

            @Override
            public void menuDetected(final MenuDetectEvent e) {
                e.doit = !isClickedOnHeader(e);
            }

            private boolean isClickedOnHeader(final MenuDetectEvent e) {
                final Rectangle clientArea = table.getClientArea();
                final Point point = table.toControl(e.x, e.y);
                return clientArea.y <= point.y && point.y <= clientArea.y + table.getHeaderHeight();
            }
        });
    }

}
