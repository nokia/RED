/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.hover.config.SimpleHoverStylingBindings;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.layer.GridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionModel;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.configs.RedTableEditBindingsConfiguration;
import org.robotframework.red.nattable.configs.SelectionLayerConfiguration;
import org.robotframework.red.nattable.configs.TreeLayerConfiguration;

import ca.odell.glazedlists.SortedList;

/**
 * @author Michal Anglart
 */
public class RedNattableLayersFactory {

    public final static int ROW_HEIGHT = 22;
    public final static int GRID_BORDER_WIDTH = 1;

    public DataLayer createDataLayer(final IDataProvider dataProvider) {
        return createDataLayer(dataProvider, new AlternatingRowConfigLabelAccumulator(),
                new AddingElementLabelAccumulator(dataProvider));
    }

    public DataLayer createDataLayer(final IDataProvider dataProvider, final IConfigLabelAccumulator... accumulators) {
        final DataLayer dataLayer = new DataLayer(dataProvider);
        dataLayer.setDefaultRowHeight(ROW_HEIGHT);
        dataLayer.setConfigLabelAccumulator(aggregatedFrom(accumulators));
        return dataLayer;
    }

    public <T> GlazedListsEventLayer<T> createGlazedListEventsLayer(final IUniqueIndexLayer dataLayer,
            final SortedList<T> sortedList) {
        return new GlazedListsEventLayer<>(dataLayer, sortedList);
    }

    private static IConfigLabelAccumulator aggregatedFrom(final IConfigLabelAccumulator... accumulators) {
        final AggregateConfigLabelAccumulator labelsAccumulator = new AggregateConfigLabelAccumulator();
        labelsAccumulator.add(accumulators);
        return labelsAccumulator;
    }

    public HoverLayer createHoverLayer(final IUniqueIndexLayer dataLayer) {
        final HoverLayer hoverLayer = new HoverLayer(dataLayer, false);
        hoverLayer.addConfiguration(new SimpleHoverStylingBindings(hoverLayer));
        return hoverLayer;
    }

    public SelectionLayer createSelectionLayer(final TableTheme theme, final IUniqueIndexLayer hoverLayer) {
        final SelectionLayer selectionLayer = new SelectionLayer(hoverLayer, false);
        ((SelectionModel) selectionLayer.getSelectionModel()).setClearSelectionOnChange(false);
        selectionLayer.addConfiguration(new SelectionLayerConfiguration());
        selectionLayer.setLayerPainter(new SelectionLayerPainter(theme.getGridBorderColor()));
        return selectionLayer;
    }

    public ViewportLayer createViewportLayer(final IUniqueIndexLayer layer) {
        return new ViewportLayer(layer);
    }

    public TreeLayer createTreeLayer(final SelectionLayer bodySelectionLayer,
            final ITreeRowModel<Object> treeRowModel) {
        final TreeLayer treeLayer = new TreeLayer(bodySelectionLayer, treeRowModel, false);
        treeLayer.addConfiguration(new TreeLayerConfiguration(treeLayer));
        return treeLayer;
    }

    public DataLayer createColumnHeaderDataLayer(final IDataProvider columnHeaderDataProvider,
            final TableTheme tableTheme) {
        return createColumnHeaderDataLayer(columnHeaderDataProvider, tableTheme, new ColumnLabelAccumulator());
    }

    public DataLayer createColumnHeaderDataLayer(final IDataProvider columnHeaderDataProvider,
            final TableTheme tableTheme, final IConfigLabelAccumulator configLabelAccumulator) {
        final DataLayer columnHeaderDataLayer = new DataLayer(columnHeaderDataProvider);
        columnHeaderDataLayer.setLayerPainter(new GridLineCellLayerPainter(tableTheme.getHeadersGridBorderColor()));
        columnHeaderDataLayer.setDefaultRowHeight(ROW_HEIGHT);
        columnHeaderDataLayer.setConfigLabelAccumulator(configLabelAccumulator);
        return columnHeaderDataLayer;
    }

    public ColumnHeaderLayer createColumnHeaderLayer(final IUniqueIndexLayer headerDataLayer,
            final SelectionLayer selectionLayer, final IUniqueIndexLayer viewportLayer) {
        return new ColumnHeaderLayer(headerDataLayer, viewportLayer, selectionLayer, false);
    }

    public <T> SortHeaderLayer<T> createSortingColumnHeaderLayer(final ColumnHeaderLayer columnHeaderLayer,
            final ISortModel sortModel) {
        return new SortHeaderLayer<>(columnHeaderLayer, sortModel);
    }

    public <T> SortHeaderLayer<T> createSortingColumnHeaderLayer(final IUniqueIndexLayer headerDataLayer,
            final ColumnHeaderLayer columnHeaderLayer, final IColumnPropertyAccessor<T> accessor,
            final IConfigRegistry configRegistry, final SortedList<T> sortedList) {
        return new SortHeaderLayer<>(columnHeaderLayer,
                new GlazedListsSortModel<>(sortedList, accessor, configRegistry, headerDataLayer));
    }

    public RowHeaderLayer createRowsHeaderLayer(final SelectionLayer selectionLayer,
            final IUniqueIndexLayer viewportLayer, final IDataProvider rowHeaderDataProvider,
            final ILayerPainter painter, final IConfigLabelAccumulator... accumulators) {
        final DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        rowHeaderDataLayer.setColumnWidthByPosition(0, 15);
        rowHeaderDataLayer.setLayerPainter(painter);
        rowHeaderDataLayer.setConfigLabelAccumulator(aggregatedFrom(accumulators));
        return new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer, false);
    }

    public CornerLayer createCornerLayer(final IDataProvider columnHeaderDataProvider, final ILayer columnHeaderLayer,
            final IDataProvider rowHeaderDataProvider, final ILayer rowHeaderLayer, final TableTheme tableTheme) {
        final DataLayer cornerDataLayer = new DataLayer(
                new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider));
        cornerDataLayer.setLayerPainter(new GridLineCellLayerPainter(tableTheme.getHeadersGridBorderColor()));
        return new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);
    }

    public GridLayer createGridLayer(final ILayer viewportLayer, final ILayer columnHeaderLayer,
            final ILayer rowHeaderLayer, final ILayer cornerLayer) {
        final GridLayer gridLayer = new GridLayer(viewportLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer, false);
        gridLayer.addConfiguration(new RedTableEditBindingsConfiguration());
        return gridLayer;
    }

    public Listener getColumnsWidthCalculatingPaintListener(final NatTable table,
            final IRowDataProvider<?> dataProvider, final DataLayer dataLayer) {
        return getColumnsWidthCalculatingPaintListener(table, dataProvider, dataLayer, 270, 270);
    }

    public Listener getColumnsWidthCalculatingPaintListener(final NatTable table,
            final IRowDataProvider<?> dataProvider, final DataLayer dataLayer, final int firstColumnWidth,
            final int secondColumnWidth) {
        return new Listener() {

            @Override
            public void handleEvent(final Event event) {

                final int tableWidth = event.width - 15; // - row header width
                final int columnCount = dataProvider.getColumnCount();

                if (columnCount == 3) {
                    final int columnWidth = tableWidth / 3;
                    dataLayer.setColumnWidthByPosition(0, columnWidth);
                    dataLayer.setColumnWidthByPosition(1, columnWidth);
                    dataLayer.setColumnWidthByPosition(2, tableWidth - (columnWidth * 2));
                } else {
                    final int commentColumnDefaultWidth = 150;

                    dataLayer.setColumnWidthByPosition(0, firstColumnWidth);
                    dataLayer.setColumnWidthByPosition(1, secondColumnWidth);
                    
                    final int argsColumnsCount = columnCount - 3;
                    final int argColumnDefaultWidth = 100;

                    final int remainingSpaceWithDefaultArgColumnsWidth = tableWidth - (firstColumnWidth
                            + secondColumnWidth + (argsColumnsCount * argColumnDefaultWidth) + commentColumnDefaultWidth);
                    int argColumnWidth = argColumnDefaultWidth;
                    if (remainingSpaceWithDefaultArgColumnsWidth > 0 && argsColumnsCount > 0) {
                        final int additionalSpaceForArgs = remainingSpaceWithDefaultArgColumnsWidth / argsColumnsCount;
                        argColumnWidth += additionalSpaceForArgs;
                    }

                    for (int i = 2; i < argsColumnsCount + 2; i++) {
                        dataLayer.setColumnWidthByPosition(i, argColumnWidth);
                    }
                    final int allColumnsWidth = firstColumnWidth + secondColumnWidth
                            + (argsColumnsCount * argColumnWidth) + commentColumnDefaultWidth;
                    if (tableWidth >= allColumnsWidth) {
                        final int remainingSpace = tableWidth
                                - (firstColumnWidth + secondColumnWidth + (argsColumnsCount * argColumnWidth));
                        dataLayer.setColumnWidthByPosition(columnCount - 1, remainingSpace);
                    } else {
                        dataLayer.setColumnWidthByPosition(columnCount - 1, commentColumnDefaultWidth);
                    }
                }

                table.removeListener(SWT.Paint, this);
            }
        };
    }
}
