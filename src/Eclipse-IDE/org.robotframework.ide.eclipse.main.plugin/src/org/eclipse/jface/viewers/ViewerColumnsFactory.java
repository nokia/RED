/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.eclipse.jface.viewers;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;

public class ViewerColumnsFactory {

    private final String name;

    private int minimumWidth;

    private int width;

    private boolean shouldShowLastSep;

    private boolean shouldExtend;

    private boolean resizable = true;

    private Image image;

    private String tooltip;

    private CellLabelProvider labelProvider;

    private boolean shouldAddEditingSupport;

    private EditingSupport editingSupport;

    private ViewerComparator ascendingComparator;

    private ViewerComparator descendingComparator;

    public static ViewerColumnsFactory newColumn(final String name) {
        return new ViewerColumnsFactory(name);
    }

    private ViewerColumnsFactory(final String name) {
        this.name = name;
    }

    public ViewerColumnsFactory withWidth(final int width) {
        this.width = width;
        return this;
    }

    public ViewerColumnsFactory shouldGrabAllTheSpaceLeft(final boolean grabSpace) {
        this.shouldExtend = grabSpace;
        return this;
    }

    public ViewerColumnsFactory shouldShowLastVerticalSeparator(final boolean shouldShowVerticalBar) {
        this.shouldShowLastSep = shouldShowVerticalBar;
        return this;
    }

    public ViewerColumnsFactory withMinWidth(final int minWidth) {
        this.minimumWidth = minWidth;
        return this;
    }

    public ViewerColumnsFactory resizable(final boolean resizable) {
        this.resizable = resizable;
        return this;
    }

    public ViewerColumnsFactory withImage(final Image image) {
        this.image = image;
        return this;
    }

    public ViewerColumnsFactory withTooltip(final String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public ViewerColumnsFactory labelsProvidedBy(final CellLabelProvider labelProvider) {
        if (labelProvider instanceof IStyledLabelProvider) {
            this.labelProvider = new TooltipsEnablingDelegatingStyledCellLabelProvider(
                    (IStyledLabelProvider) labelProvider);
        } else {
            this.labelProvider = labelProvider;
        }
        return this;
    }

    public ViewerColumnsFactory editingEnabled() {
        this.shouldAddEditingSupport = true;
        return this;
    }

    public ViewerColumnsFactory editingSupportedBy(final EditingSupport editingSupport) {
        this.editingSupport = editingSupport;
        return this;
    }

    public ViewerColumnsFactory editingEnabledOnlyWhen(final boolean condition) {
        this.shouldAddEditingSupport = condition;
        return this;
    }

    public ViewerColumnsFactory equipWithThreeWaySorting(final ViewerComparator ascendingComparator,
            final ViewerComparator descendingComparator) {
        this.ascendingComparator = ascendingComparator;
        this.descendingComparator = descendingComparator;
        return this;
    }

    public ViewerColumnsFactory equipWithThreeWaySorting(final ViewerComparator ascendingComparator) {
        return equipWithThreeWaySorting(ascendingComparator, new ReversingViewerComparator(ascendingComparator));
    }

    public void createFor(final TableViewer viewer) {
        final TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
        column.getColumn().setWidth(Math.max(width, minimumWidth));
        column.getColumn().setText(name);
        column.getColumn().setImage(image);
        column.getColumn().addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(final DisposeEvent e) {
                if (image != null) {
                    image.dispose();
                }
            }
        });
        column.getColumn().setToolTipText(tooltip);
        column.getColumn().setResizable(resizable);
        if (shouldExtend) {
            column.getColumn().setData("autoExtending", Boolean.TRUE);
            final ControlAdapter resizeListener = createResizeListener(viewer);
            column.getColumn().addDisposeListener(new DisposeListener() {

                @Override
                public void widgetDisposed(final DisposeEvent e) {
                    viewer.getTable().removeControlListener(resizeListener);
                }
            });
            viewer.getTable().addControlListener(resizeListener);
        }

        if (labelProvider != null) {
            column.setLabelProvider(labelProvider);
        }
        if (shouldAddEditingSupport) {
            column.setEditingSupport(editingSupport);
        }

        final SelectionListener createSortSwitchingListener = createSortSwitchingListener(viewer);
        if (createSortSwitchingListener != null) {
            column.getColumn().addSelectionListener(createSortSwitchingListener);
        }
    }

    private ControlAdapter createResizeListener(final TableViewer viewer) {
        return new ControlAdapter() {

            @Override
            public void controlResized(final ControlEvent e) {
                resizeColumnToWholeViewer(viewer);
            }
        };
    }

    private void resizeColumnToWholeViewer(final TableViewer viewer) {
        final int totalTableWidth = viewer.getControl().getSize().x;

        TableColumn autoExtendingColumn = null;
        for (final TableColumn currentColumn : viewer.getTable().getColumns()) {
            if (Boolean.TRUE.equals(currentColumn.getData("autoExtending"))) {
                autoExtendingColumn = currentColumn;
            }
        }
        if (autoExtendingColumn == null) {
            return;
        }

        int otherColumnsTotalWidth = 0;
        for (final TableColumn currentColumn : viewer.getTable().getColumns()) {
            if (currentColumn != autoExtendingColumn) {
                otherColumnsTotalWidth += currentColumn.getWidth();
            }
        }
        final int scrollbarWidth = getScrollBarWidth(viewer);

        final int borderWidth = viewer.getTable().getBorderWidth();
        final int additional = shouldShowLastSep ? 1 : 0;
        final int widthToOccupy = totalTableWidth
                - (otherColumnsTotalWidth + scrollbarWidth + 2 * borderWidth + additional);
        autoExtendingColumn.setWidth(Math.max(minimumWidth, widthToOccupy));
    }

    private int getScrollBarWidth(final TableViewer viewer) {
        final ScrollBar verticalBar = viewer.getTable().getVerticalBar();
        return verticalBar == null || !verticalBar.isVisible() ? 0 : verticalBar.getSize().x;
    }

    private SelectionListener createSortSwitchingListener(final TableViewer viewer) {
        if (ascendingComparator == null | ascendingComparator == null) {
            return null;
        }
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final TableColumn currentSortColumn = viewer.getTable().getSortColumn();
                final TableColumn clickedColumn = (TableColumn) e.getSource();

                int nextDirection;
                if (currentSortColumn == clickedColumn) {
                    nextDirection = getNextDirection(viewer.getTable().getSortDirection());
                } else {
                    nextDirection = getNextDirection(SWT.NONE);
                }
                viewer.getTable().setSortDirection(nextDirection);
                viewer.getTable().setSortColumn(nextDirection == SWT.NONE ? null : clickedColumn);
                viewer.setComparator(getNextComparator(nextDirection, ascendingComparator, descendingComparator));
            }
        };
    }

    private SelectionListener createSortSwitchingListener(final TreeViewer viewer) {
        if (ascendingComparator == null | ascendingComparator == null) {
            return null;
        }
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final TreeColumn currentSortColumn = viewer.getTree().getSortColumn();
                final TreeColumn clickedColumn = (TreeColumn) e.getSource();

                int nextDirection;
                if (currentSortColumn == clickedColumn) {
                    nextDirection = getNextDirection(viewer.getTree().getSortDirection());
                } else {
                    nextDirection = getNextDirection(SWT.NONE);
                }
                viewer.getTree().setSortDirection(nextDirection);
                viewer.getTree().setSortColumn(nextDirection == SWT.NONE ? null : clickedColumn);
                viewer.setComparator(getNextComparator(nextDirection, ascendingComparator, descendingComparator));
            }
        };
    }

    private static ViewerComparator getNextComparator(final int direction, final ViewerComparator ascendingComparator,
            final ViewerComparator descendingComparator) {
        if (direction == SWT.NONE) {
            return null;
        } else if (direction == SWT.UP) {
            return ascendingComparator;
        } else if (direction == SWT.DOWN) {
            return descendingComparator;
        }
        throw new IllegalArgumentException("Unrecognized sorting direction: " + direction);
    }

    private static int getNextDirection(final int direction) {
        if (direction == SWT.NONE) {
            return SWT.UP;
        } else if (direction == SWT.UP) {
            return SWT.DOWN;
        } else if (direction == SWT.DOWN) {
            return SWT.NONE;
        }
        throw new IllegalArgumentException("Unrecognized sorting direction : " + direction);
    }

    public TreeViewerColumn createFor(final TreeViewer viewer) {
        final TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
        column.getColumn().setWidth(Math.max(width, minimumWidth));
        column.getColumn().setText(name);
        column.getColumn().setImage(image);
        column.getColumn().addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(final DisposeEvent e) {
                if (image != null) {
                    image.dispose();
                }
            }
        });
        column.getColumn().setToolTipText(tooltip);
        column.getColumn().setResizable(resizable);

        if (shouldExtend) {
            column.getColumn().setData("autoExtending", Boolean.TRUE);
            final ControlAdapter resizeListener = createResizeListener(viewer);
            column.getColumn().addDisposeListener(new DisposeListener() {

                @Override
                public void widgetDisposed(final DisposeEvent e) {
                    viewer.getTree().removeControlListener(resizeListener);
                }
            });
            viewer.getTree().addControlListener(resizeListener);
        }

        if (labelProvider != null) {
            column.setLabelProvider(labelProvider);
        }
        if (shouldAddEditingSupport) {
            column.setEditingSupport(editingSupport);
        }

        final SelectionListener createSortSwitchingListener = createSortSwitchingListener(viewer);
        if (createSortSwitchingListener != null) {
            column.getColumn().addSelectionListener(createSortSwitchingListener);
        }
        return column;
    }

    private ControlAdapter createResizeListener(final TreeViewer viewer) {
        return new ControlAdapter() {

            @Override
            public void controlResized(final ControlEvent e) {
                resizeColumnToWholeViewer(viewer);
            }
        };
    }

    private void resizeColumnToWholeViewer(final TreeViewer viewer) {
        final int totalTableWidth = viewer.getControl().getSize().x;

        TreeColumn autoExtendingColumn = null;
        for (final TreeColumn currentColumn : viewer.getTree().getColumns()) {
            if (Boolean.TRUE.equals(currentColumn.getData("autoExtending"))) {
                autoExtendingColumn = currentColumn;
            }
        }
        if (autoExtendingColumn == null) {
            return;
        }

        int otherColumnsTotalWidth = 0;
        for (final TreeColumn currentColumn : viewer.getTree().getColumns()) {
            if (currentColumn != autoExtendingColumn) {
                otherColumnsTotalWidth += currentColumn.getWidth();
            }
        }
        final int scrollbarWidth = getScrollBarWidth(viewer);

        final int borderWidth = viewer.getTree().getBorderWidth();
        final int additional = shouldShowLastSep ? 1 : 0;
        final int widthToOccupy = totalTableWidth
                - (otherColumnsTotalWidth + scrollbarWidth + 2 * borderWidth + additional);
        autoExtendingColumn.setWidth(Math.max(minimumWidth, widthToOccupy));
    }

    private int getScrollBarWidth(final TreeViewer viewer) {
        final ScrollBar verticalBar = viewer.getTree().getVerticalBar();
        return verticalBar == null || !verticalBar.isVisible() ? 0 : verticalBar.getSize().x;
    }

    private static class ReversingViewerComparator extends ViewerComparator {

        private final ViewerComparator wrappedComparator;

        ReversingViewerComparator(final ViewerComparator comparator) {
            this.wrappedComparator = comparator;
        }

        @Override
        public int category(final Object element) {
            return wrappedComparator.category(element);
        }

        @Override
        public int compare(final Viewer viewer, final Object e1, final Object e2) {
            final int signReverser = -1;
            return signReverser * wrappedComparator.compare(viewer, e1, e2);
        }

        @Override
        public boolean isSorterProperty(final Object element, final String property) {
            return wrappedComparator.isSorterProperty(element, property);
        }
    }
}
