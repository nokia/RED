/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.ITableHyperlinksDetector;
import org.robotframework.red.nattable.TableCellStringData;
import org.robotframework.red.nattable.TableCellsStrings;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.StructuredContentProvider;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;

public class TableHyperlinksSupport {

    private final NatTable table;

    private final TableCellsStrings tableStrings;

    private final List<ITableHyperlinksDetector> detectors = new ArrayList<>();

    private Shell infoShell;
    private List<IHyperlink> hyperlinks = new ArrayList<>();
    private TableCellStringData currentData = null;
    private Cursor cursor = null;

    public static TableHyperlinksSupport enableHyperlinksInTable(final NatTable table,
            final TableCellsStrings tableStrings) {

        final TableHyperlinksSupport detector = new TableHyperlinksSupport(table, tableStrings);
        table.addMouseListener(detector.new HyperlinksClickListener());
        table.addMouseMoveListener(detector.new HyperlinksMouseMoveListener());

        final Display display = table.getDisplay();
        final HyperlinksKeyListener filter = detector.new HyperlinksKeyListener();
        display.addFilter(SWT.KeyUp, filter);
        table.addDisposeListener(e -> display.removeFilter(SWT.KeyUp, filter));
        return detector;
    }

    private TableHyperlinksSupport(final NatTable table, final TableCellsStrings tableStrings) {
        this.tableStrings = tableStrings;
        this.table = table;
    }

    public void addDetectors(final ITableHyperlinksDetector... detectors) {
        this.detectors.addAll(newArrayList(detectors));
    }

    public void removeDetector(final ITableHyperlinksDetector detector) {
        this.detectors.remove(detector);
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

    private void openHyperlink(final IHyperlink linkToOpen) {
        removeHyperlink();

        SwtThread.asyncExec(new Runnable() {

            @Override
            public void run() {
                linkToOpen.open();
            }
        });
    }

    public List<ITableHyperlinksDetector> getDetectors() {
        return detectors;
    }

    @VisibleForTesting
    static Optional<IRegion> getMergedHyperlinkRegion(final Collection<IHyperlink> hyperlinks) {
        if (hyperlinks.isEmpty()) {
            return Optional.empty();
        }
        IRegion hyperlinkRegion = Iterables.getFirst(hyperlinks, null).getHyperlinkRegion();
        for (final IHyperlink link : hyperlinks) {
            hyperlinkRegion = merge(hyperlinkRegion, link.getHyperlinkRegion());
        }
        return Optional.of(hyperlinkRegion);
    }

    private static IRegion merge(final IRegion region1, final IRegion region2) {
        final int startOffset = Integer.min(region1.getOffset(), region2.getOffset());
        final int endOffset = Integer.max(region1.getOffset() + region1.getLength(),
                region2.getOffset() + region2.getLength());
        return new Region(startOffset, endOffset - startOffset);
    }

    private class HyperlinksKeyListener implements Listener {

        @Override
        public void handleEvent(final Event event) {
            if (event.keyCode == SWT.CTRL) {
                removeHyperlink();
            }
        }
    }

    private class HyperlinksClickListener extends MouseAdapter {

        @Override
        public void mouseUp(final MouseEvent e) {
            if (!hyperlinks.isEmpty()) {
                openHyperlink(hyperlinks.get(0));
            }
        }
    }

    private class HyperlinksMouseMoveListener implements MouseMoveListener {

        @Override
        public void mouseMove(final MouseEvent e) {

            if (e.stateMask != SWT.CTRL || detectors.isEmpty()) {
                // no detectors or CTRL is not pressed
                removeHyperlink();
                return;
            }

            final int column = table.getColumnPositionByX(e.x);
            final int row = table.getRowPositionByY(e.y);
            final ILayerCell cell = table.getCellByPosition(column, row);
            if (cell == null) {
                // no cell for given table coordinates
                removeHyperlink();
                return;
            }
            final String actualLabel = (String) cell.getDataValue();

            final TableCellStringData textData = tableStrings.get(column, row);
            if (textData == null) {
                // no info about labels drawn in this cell
                removeHyperlink();
                return;
            }

            final int index = textData.getCharacterIndexFrom(e.x, e.y);
            if (index < 0) {
                // mouse position is outside of drawn label

                if (isPopupOpen()) {
                    final Point popupLocation = infoShell.getLocation();
                    final Point popupSize = infoShell.getSize();
                    final Rectangle popupRectangle = new Rectangle(popupLocation.x, popupLocation.y, popupSize.x,
                            popupSize.y);

                    final Point labelLocation = table.toDisplay(textData.getCoordinate());
                    final Rectangle labelRectangle = new Rectangle(labelLocation.x, labelLocation.y,
                            textData.getExtent().x, textData.getExtent().y);

                    if (!popupRectangle.union(labelRectangle).contains(table.toDisplay(e.x, e.y))) {
                        // mouse is moving outside of popup, so we need to close and remove
                        removeHyperlink();
                    }
                } else {
                    removeHyperlink();
                }
                return;
            }

            final Range<Integer> currentHyperlinkRegion = textData.getHyperlinkRegion();
            if (currentHyperlinkRegion != null && currentHyperlinkRegion.lowerEndpoint() <= index
                    && index <= currentHyperlinkRegion.upperEndpoint()) {
                // no need to remove hyperlinks, we're moving inside place which already has link
                return;
            } else if (currentHyperlinkRegion != null && isPopupOpen()) {
                // we're over the label, outside the generated link, but the popup is open, so we
                // don't want to recalculate hyperlinks
                return;
            }

            hyperlinks = collectHyperlinks(column, row, actualLabel, index);

            final Optional<IRegion> hyperlinkRegion = getMergedHyperlinkRegion(hyperlinks);
            if (!hyperlinkRegion.isPresent()) {
                // there is no hyperlink region
                removeHyperlink();
                return;
            }

            if (currentData != null) {
                currentData.removeHyperlink();
            }
            textData.createHyperlinkAt(hyperlinkRegion.get().getOffset(),
                    hyperlinkRegion.get().getOffset() + hyperlinkRegion.get().getLength());
            currentData = textData;


            if (hyperlinks.size() > 1) {
                openChoicePopup(calculatePopupLocation(cell, textData));
            }
            changeCursor();
            table.redraw();
        }

        private boolean isPopupOpen() {
            return infoShell != null && !infoShell.isDisposed() && infoShell.isVisible();
        }

        private List<IHyperlink> collectHyperlinks(final int column, final int row, final String actualLabel,
                final int index) {
            // 1 is substracted due to column/row headers
            final List<IHyperlink> hyperlinks = new ArrayList<>();
            for (final ITableHyperlinksDetector detector : detectors) {
                hyperlinks.addAll(detector.detectHyperlinks(row - 1, column - 1, actualLabel, index));
            }
            return hyperlinks;
        }

        private Point calculatePopupLocation(final ILayerCell cell, final TableCellStringData textData) {
            final int x = textData.getCoordinate().x;
            final int y = cell.getBounds().y + cell.getBounds().height;
            return table.toDisplay(x, y);
        }

        private void openChoicePopup(final Point location) {
            if (infoShell != null && !infoShell.isDisposed()) {
                infoShell.close();
                infoShell.dispose();
            }
            infoShell = new Shell(table.getShell(), SWT.TOOL | SWT.ON_TOP);
            infoShell.setLocation(location);
            GridLayoutFactory.fillDefaults().applyTo(infoShell);

            final Composite comp = new Composite(infoShell, SWT.NONE);
            comp.setBackground(table.getBackground());
            GridLayoutFactory.fillDefaults().margins(3, 5).applyTo(comp);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(comp);

            final TableViewer viewer = new TableViewer(comp, SWT.SINGLE | SWT.NO_SCROLL);
            viewer.getTable().setHeaderVisible(false);
            viewer.getTable().setLinesVisible(false);
            viewer.setContentProvider(new HyperlinksContentProvider());
            viewer.getTable().addMouseMoveListener(new MouseMoveListener() {

                @Override
                public void mouseMove(final MouseEvent e) {
                    if (viewer.getTable().equals(e.getSource())) {
                        final TableItem item = viewer.getTable().getItem(new Point(e.x, e.y));
                        if (item != null) {
                            viewer.getTable().setSelection(new TableItem[] { item });
                        }
                    }
                }
            });
            viewer.getTable().addMouseListener(new MouseAdapter() {

                @Override
                public void mouseUp(final MouseEvent e) {
                    if (viewer.getTable().getSelectionCount() < 1 || e.button != 1) {
                        return;
                    }

                    if (viewer.getTable().equals(e.getSource())) {
                        final TableItem item = viewer.getTable().getItem(new Point(e.x, e.y));
                        final TableItem selection = viewer.getTable().getSelection()[0];
                        if (selection.equals(item)) {
                            openHyperlink((IHyperlink) item.getData());
                        }
                    }
                }
            });
            viewer.getTable().addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetDefaultSelected(final SelectionEvent e) {
                    openHyperlink(Selections.getSingleElement((IStructuredSelection) viewer.getSelection(),
                            IHyperlink.class));
                }
            });

            ViewerColumnsFactory.newColumn("")
                    .labelsProvidedBy(new HyperlinksLabelProvider())
                    .shouldGrabAllTheSpaceLeft(true)
                    .withMinWidth(50)
                    .createFor(viewer);

            viewer.setInput(hyperlinks);
            GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());

            viewer.getTable().select(0);
            viewer.getTable().getColumn(0).pack();
            viewer.getTable().pack();

            infoShell.pack();
            infoShell.setVisible(true);
        }

        private void changeCursor() {
            if (cursor == null) {
                cursor = new Cursor(table.getDisplay(), SWT.CURSOR_HAND);
            }
            table.getShell().setCursor(cursor);
        }
    }

    private static class HyperlinksContentProvider extends StructuredContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            final List<?> hyperlinks = (List<?>) inputElement;
            return hyperlinks.toArray();
        }
    }

    private static class HyperlinksLabelProvider extends RedCommonLabelProvider {
        @Override
        public StyledString getStyledText(final Object element) {
            final IHyperlink hyperlink = (IHyperlink) element;
            return new StyledString(hyperlink.getHyperlinkText());
        }
    }
}