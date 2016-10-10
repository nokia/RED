/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Shell;
import org.robotframework.red.nattable.TableCellStringData;
import org.robotframework.red.nattable.TableCellsStrings;

import com.google.common.collect.Range;

public class TableHyperlinksSupport {

    private final NatTable table;

    private final TableCellsStrings tableStrings;

    private final List<ITableHyperlinksDetector> detectors = new ArrayList<>();

    private Shell infoShell;
    private final List<IHyperlink> hyperlinks = new ArrayList<>();
    private TableCellStringData currentData = null;
    private Cursor cursor = null;

    public static TableHyperlinksSupport enableHyperlinksInTable(final NatTable table,
            final TableCellsStrings tableStrings) {

        final TableHyperlinksSupport detector = new TableHyperlinksSupport(table, tableStrings);
        table.addMouseListener(detector.new HyperlinksClickListener());
        table.addMouseMoveListener(detector.new HyperlinksMouseMoveListener());
        table.addKeyListener(detector.new HyperlinksKeyListener());
        return detector;
    }

    private TableHyperlinksSupport(final NatTable table, final TableCellsStrings tableStrings) {
        this.tableStrings = tableStrings;
        this.table = table;
    }

    public void addDetectors(final ITableHyperlinksDetector... detectors) {
        this.detectors.addAll(newArrayList(detectors));
    }

    private void removeHyperlink() {
        if (infoShell != null && !infoShell.isDisposed()) {
            infoShell.close();
            infoShell.dispose();
            infoShell = null;
        }
        if (cursor != null) {
            table.getShell().setCursor(null);
            cursor.dispose();
            cursor = null;
        }
        if (currentData != null) {
            currentData.removeHyperlink();
            table.redraw();
        }
        hyperlinks.clear();
    }

    private static IRegion merge(final IRegion region1, final IRegion region2) {
        return new Region(Integer.min(region1.getOffset(), region2.getOffset()),
                Integer.max(region1.getLength(), region2.getLength()));
    }

    private class HyperlinksKeyListener extends KeyAdapter {

        @Override
        public void keyReleased(final KeyEvent e) {
            if (e.keyCode == SWT.CTRL) {
                removeHyperlink();
            }
        }
    }

    private class HyperlinksClickListener extends MouseAdapter {

        @Override
        public void mouseUp(final MouseEvent e) {
            if (hyperlinks.isEmpty()) {
                return;
            }
            if (infoShell != null && !infoShell.isDisposed()) {
                infoShell.close();
                infoShell.dispose();
            }
            hyperlinks.get(0).open();
        }
    }

    private class HyperlinksMouseMoveListener implements MouseMoveListener {

        @Override
        public void mouseMove(final MouseEvent e) {
            if (/* table.isFocusControl() && */ e.stateMask == SWT.CTRL && !detectors.isEmpty()) {
                final int column = table.getColumnPositionByX(e.x);
                final int row = table.getRowPositionByY(e.y);

                final TableCellStringData textData = tableStrings.get(column, row);
                if (textData == null) {
                    removeHyperlink();
                    return;
                }
                final int index = textData.getCharacterIndexFrom(e.x, e.y);
                if (index < 0) {
                    removeHyperlink();
                    return;
                }
                final Range<Integer> currentHyperlinkRegion = textData.getHyperlinkRegion();
                if (currentHyperlinkRegion != null && currentHyperlinkRegion.lowerEndpoint() <= index
                        && index <= currentHyperlinkRegion.upperEndpoint()) {
                    // no need to remove hyperlinks, we're still in the same place
                    return;
                }

                hyperlinks.clear();
                for (final ITableHyperlinksDetector detector : detectors) {
                    // 1 is substracted due to column/row headers
                    hyperlinks.addAll(detector.detectHyperlinks(row - 1, column - 1, textData.getString(), index));
                }
                if (hyperlinks.isEmpty()) {
                    removeHyperlink();
                    return;
                }

                IRegion hyperlinkRegion = null;
                for (final IHyperlink link : hyperlinks) {
                    if (hyperlinkRegion == null) {
                        hyperlinkRegion = link.getHyperlinkRegion();
                    } else {
                        hyperlinkRegion = merge(hyperlinkRegion, link.getHyperlinkRegion());
                    }
                }

                if (hyperlinkRegion == null) {
                    removeHyperlink();
                    return;
                }
                textData.createHyperlinkAt(hyperlinkRegion.getOffset(),
                        hyperlinkRegion.getOffset() + hyperlinkRegion.getLength());

                if (hyperlinks.size() > 1) {
                    if (infoShell != null && !infoShell.isDisposed()) {
                        infoShell.close();
                        infoShell.dispose();
                    }
                    // infoShell = new Shell(table.getShell(), SWT.TOOL | SWT.ON_TOP);
                    // infoShell.setSize(50, 50);
                    // infoShell.open();
                }

                if (cursor == null) {
                    cursor = new Cursor(table.getDisplay(), SWT.CURSOR_HAND);
                }
                table.getShell().setCursor(cursor);
                table.redraw();

                currentData = textData;
            } else {
                removeHyperlink();
            }
        }
    }
}