/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

public class Viewers {

    /**
     * Takes care of given context activation and deactivation on viewer focus changes.
     * 
     * @param viewer
     * @param site
     * @param contextId
     */
    public static void boundViewerWithContext(final ColumnViewer viewer, final IWorkbenchSite site,
            final String contextId) {
        viewer.getControl().addFocusListener(new ContextActivatingFocusListener(contextId, site));
    }

    /**
     * Viewers maintain indexes based on columns create orders, not their
     * position. This method is able to give current column position based on
     * creation order position.
     * 
     * @param viewer
     * @param createOrderIndex
     * @return
     */
    public static int createOrderIndexToPositionIndex(final ColumnViewer viewer, final int createOrderIndex) {
        return createOrderIndexToPositionIndex(getColumnOrder(viewer), createOrderIndex);
    }

    private static int createOrderIndexToPositionIndex(final int[] columnOrder, final int createOrderIndex) {
        for (int i = 0; i < columnOrder.length; i++) {
            if (columnOrder[i] == createOrderIndex) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unable to find column with creation order index " + createOrderIndex);
    }

    /**
     * Viewers maintain indexes based on columns create orders, not their
     * position. This method is able to give create order index basing on
     * creation order index.
     * 
     * @param positionIndex
     * @return
     */
    public static int positionIndexToCreateOrderIndex(final ColumnViewer viewer, final int positionIndex) {
        return positionIndexToCreateOrderIndex(getColumnOrder(viewer), positionIndex);
    }

    /**
     * Viewers maintain indexes based on columns create orders, not their
     * position. This method is able to give create order index basing on
     * creation order index.
     * 
     * @param positionIndex
     * @return
     */
    private static int positionIndexToCreateOrderIndex(final int[] columnOrder, final int positionIndex) {
        return columnOrder[positionIndex];
    }

    private static int[] getColumnOrder(final ColumnViewer viewer) {
        if (viewer instanceof TreeViewer) {
            return ((TreeViewer) viewer).getTree().getColumnOrder();
        } else if (viewer instanceof TableViewer) {
            return ((TableViewer) viewer).getTable().getColumnOrder();
        }
        throw new IllegalStateException("Unrecognized viewer type: "
                + (viewer == null ? "null" : viewer.getClass().getName()));
    }

    private static class ContextActivatingFocusListener implements FocusListener {
        private final String contextId;
        private final IWorkbenchSite site;
        private IContextActivation activationToken = null;

        private ContextActivatingFocusListener(final String contextId, final IWorkbenchSite site) {
            this.contextId = contextId;
            this.site = site;
        }

        @Override
        public void focusLost(final FocusEvent e) {
            getContextService(site).deactivateContext(activationToken);
        }

        @Override
        public void focusGained(final FocusEvent e) {
            activationToken = getContextService(site).activateContext(contextId);
        }

        private IContextService getContextService(final IWorkbenchSite site) {
            return site.getService(IContextService.class);
        }
    }
}
