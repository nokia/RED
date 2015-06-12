package org.eclipse.jface.viewers;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.TableColumn;

public class ViewerColumnsFactory {

    private final String name;
    private int width;
    private CellLabelProvider labelProvider;
    private boolean resizable = true;
    private Image image;
    private String tooltip;
    private EditingSupport editingSupport;
    private boolean shouldAddEditingSupport;
    private boolean shouldExtend;
    private int minimumWidth;

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

    public ViewerColumnsFactory editingSupportedBy(final EditingSupport editingSupport) {
        this.editingSupport = editingSupport;
        return this;
    }

    public ViewerColumnsFactory editingEnabledOnlyWhen(final boolean condition) {
        this.shouldAddEditingSupport = condition;
        return this;
    }

    public TableViewerColumn createFor(final TableViewer viewer) {
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
            final ControlAdapter resizeListener = createResizeListener(viewer, column.getColumn());
            column.getColumn().addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(final DisposeEvent e) {
                    viewer.getTable().removeControlListener(resizeListener);
                }
            });
            viewer.getTable().addControlListener(resizeListener);
        }

        column.setLabelProvider(labelProvider);
        if (shouldAddEditingSupport) {
            column.setEditingSupport(editingSupport);
        }
        return column;
    }

    private ControlAdapter createResizeListener(final TableViewer viewer, final TableColumn column) {
        return new ControlAdapter() {
            @Override
            public void controlResized(final ControlEvent e) {
                final int totalTableWidth = viewer.getTable().getSize().x;

                int otherColumnsTotalWidth = 0;
                for (final TableColumn currentColumn : viewer.getTable().getColumns()) {
                    if (currentColumn != column) {
                        otherColumnsTotalWidth += currentColumn.getWidth();
                    }
                }
                final int scrollbarWidth = getScrollBarWidth(viewer);

                final int widthToOccupy = totalTableWidth - (otherColumnsTotalWidth + scrollbarWidth);
                column.setWidth(Math.max(minimumWidth, widthToOccupy));
            }

            private int getScrollBarWidth(final TableViewer viewer) {
                final ScrollBar verticalBar = viewer.getTable().getVerticalBar();
                return verticalBar == null || !verticalBar.isVisible() ? 0 : verticalBar.getSize().x;
            }
        };
    }

    public TreeViewerColumn createFor(final TreeViewer viewer) {
        final TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
        column.getColumn().setWidth(width);
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
        column.setLabelProvider(labelProvider);
        if (shouldAddEditingSupport) {
            column.setEditingSupport(editingSupport);
        }
        return column;
    }
}
