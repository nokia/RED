/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.eclipse.jface.viewers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.robotframework.red.viewers.Viewers;

import com.google.common.primitives.Ints;

public class RowExposingTableViewer extends TableViewer {

    public RowExposingTableViewer(final Composite parent, final int style) {
        super(parent, style);
    }

    @Override
    public ViewerRow getViewerRowFromItem(final Widget item) { // changing protected to public
        return super.getViewerRowFromItem(item);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List getSelectionFromWidget() {
        return super.getSelectionFromWidget();
    }

    public void packFirstColumn() {
        getTable().getColumn(0).pack();
    }

    public void removeColumnAtPosition(final int position) {
        getTable().getColumn(Viewers.positionIndexToCreateOrderIndex(this, position)).dispose();
    }

    public void moveLastColumnTo(final int position) {
        final List<Integer> order = new LinkedList<Integer>(Ints.asList(getTable().getColumnOrder()));
        final Integer last = order.remove(order.size() - 1);
        order.add(position, last);
        final int[] newOrder = Ints.toArray(order);
        getTable().setColumnOrder(newOrder);
    }

    public void reflowColumnsWidth() {
        // when column is configured using ViewerColumnsFactory it may register
        // resize listener which ensures that the column is always occupying all
        // the space available

        // here we are sending fake resize event which causes such columns to
        // recalculate the space which should be occupied
        getTable().notifyListeners(SWT.Resize, null);
    }

    public void setFocusCell(final int index) {
        try {
            // This ugly workaround is due to the lack of API for setting
            // focused cell
            // the code is taken from
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=198260
            // and seem to be the only reasonable idea...

            final TableItem selectedItem = getTable().getSelection()[0];
            final ViewerRow viewerRow = getViewerRowFromItem(selectedItem);
            final ViewerCell cell = viewerRow.getCell(index);
            final Method setFocusCell = TableViewerFocusCellManager.class.getSuperclass().getDeclaredMethod(
                    "setFocusCell", ViewerCell.class);
            setFocusCell.setAccessible(true);

            final Field fcmField = TableViewerEditor.class.getDeclaredField("focusCellManager");
            fcmField.setAccessible(true);
            final Object fcm = fcmField.get(getColumnViewerEditor());

            setFocusCell.invoke(fcm, cell);
            setSelection(new StructuredSelection(viewerRow.getItem().getData()));
        } catch (final Exception e) {
            // we were unable to
        }
    }
}