/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
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
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayerPainter;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.nattable.configs.LeftRightMoveOnEnterConfiguration;
import org.robotframework.red.nattable.configs.RedTableEditBindingsConfiguration;

import ca.odell.glazedlists.SortedList;

/**
 * @author Michal Anglart
 *
 */
public class RedNattableLayersFactory {

    public final static int ROW_HEIGHT = 22;

    public DataLayer createDataLayer(final IDataProvider dataProvider) {
        return createCustomDataLayer(dataProvider, 270, 270);
    }
    
    public DataLayer createDataLayer(final IDataProvider dataProvider, final int firstColumnWidth,
            final int secondColumnWidth) {
        return createCustomDataLayer(dataProvider, firstColumnWidth, secondColumnWidth,
                new AlternatingRowConfigLabelAccumulator(), new AddingElementLabelAccumulator(dataProvider));
    }

    public DataLayer createCustomDataLayer(final IDataProvider dataProvider, final int firstColumnWidth,
            final int secondColumnWidth, final IConfigLabelAccumulator... accumulators) {
        final DataLayer dataLayer = new DataLayer(dataProvider);
        dataLayer.setColumnPercentageSizing(2, true);
        dataLayer.setColumnWidthByPosition(0, firstColumnWidth);
        dataLayer.setColumnWidthByPosition(1, secondColumnWidth);
        dataLayer.setDefaultRowHeight(ROW_HEIGHT);

        dataLayer.setConfigLabelAccumulator(aggregatedFrom(accumulators));
        return dataLayer;
    }

    public <T> GlazedListsEventLayer<T> createGlazedListEventsLayer(final IUniqueIndexLayer dataLayer,
            final SortedList<T> sortedList) {
        return new GlazedListsEventLayer<T>(dataLayer, sortedList);
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
        final SelectionLayer selectionLayer = new SelectionLayer(hoverLayer);
        selectionLayer.addConfiguration(new LeftRightMoveOnEnterConfiguration());
        selectionLayer.setLayerPainter(new SelectionLayerPainter(theme.getGridBorderColor()));
        return selectionLayer;
    }

    public ViewportLayer createViewportLayer(final SelectionLayer selectionLayer) {
        return new ViewportLayer(selectionLayer);
    }

    public ColumnHeaderLayer createColumnHeaderLayer(final SelectionLayer selectionLayer,
            final IUniqueIndexLayer viewportLayer, final IDataProvider columnHeaderDataProvider) {
        final DataLayer columnHeaderDataLayer = new DataLayer(columnHeaderDataProvider);
        columnHeaderDataLayer.setDefaultRowHeight(ROW_HEIGHT);
        return new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer, false);
    }

    public <T> SortHeaderLayer<T> createSortingColumnHeaderLayer(final SelectionLayer selectionLayer,
            final IUniqueIndexLayer viewportLayer, final IDataProvider columnHeaderDataProvider,
            final SortedList<T> sortedList, final IConfigRegistry configRegistry,
            final IColumnPropertyAccessor<T> accessor) {

        final DataLayer columnHeaderDataLayer = new DataLayer(columnHeaderDataProvider);
        columnHeaderDataLayer.setDefaultRowHeight(ROW_HEIGHT);
        columnHeaderDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

        final ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer,
                selectionLayer, false);

        return new SortHeaderLayer<>(columnHeaderLayer,
                new GlazedListsSortModel<>(sortedList, accessor, configRegistry, columnHeaderDataLayer));
    }

    public DataLayer createColumnHeaderDataLayer(final IDataProvider columnHeaderDataProvider) {
        return createColumnHeaderDataLayer(columnHeaderDataProvider, new ColumnLabelAccumulator());
    }
    
    public DataLayer createColumnHeaderDataLayer(final IDataProvider columnHeaderDataProvider, final IConfigLabelAccumulator configLabelAccumulator) {
        final DataLayer columnHeaderDataLayer = new DataLayer(columnHeaderDataProvider);
        columnHeaderDataLayer.setDefaultRowHeight(ROW_HEIGHT);
        columnHeaderDataLayer.setConfigLabelAccumulator(configLabelAccumulator);
        return columnHeaderDataLayer;
    }

    public ColumnHeaderLayer createColumnHeaderLayer(final IUniqueIndexLayer headerDataLayer,
            final SelectionLayer selectionLayer, final IUniqueIndexLayer viewportLayer) {
        return new ColumnHeaderLayer(headerDataLayer, viewportLayer, selectionLayer, false);
    }

    public <T> SortHeaderLayer<T> createSortingColumnHeaderLayer(final IUniqueIndexLayer headerDataLayer,
            final ColumnHeaderLayer columnHeaderLayer, final IColumnPropertyAccessor<T> accessor,
            final IConfigRegistry configRegistry, final SortedList<T> sortedList) {
        return new SortHeaderLayer<>(columnHeaderLayer,
                new GlazedListsSortModel<>(sortedList, accessor, configRegistry, headerDataLayer));
    }

    public RowHeaderLayer createRowsHeaderLayer(final SelectionLayer selectionLayer,
            final IUniqueIndexLayer viewportLayer, final IDataProvider rowHeaderDataProvider) {
        final DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        rowHeaderDataLayer.setColumnWidthByPosition(0, 15);
        rowHeaderDataLayer.setLayerPainter(new SelectionLayerPainter(ColorsManager.getColor(250, 250, 250)));
        return new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer, false);
    }

    public CornerLayer createCornerLayer(final IDataProvider columnHeaderDataProvider, final ILayer columnHeaderLayer,
            final IDataProvider rowHeaderDataProvider, final ILayer rowHeaderLayer) {
        final DataLayer columnHeaderDataLayer = new DataLayer(
                new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider));
        return new CornerLayer(columnHeaderDataLayer, rowHeaderLayer, columnHeaderLayer);
    }
    
    public GridLayer createGridLayer(final ILayer viewportLayer, final ILayer columnHeaderLayer,
            final ILayer rowHeaderLayer, final ILayer cornerLayer) {
        final GridLayer gridLayer = new GridLayer(viewportLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer, false);
        gridLayer.addConfiguration(new RedTableEditBindingsConfiguration());
        return gridLayer;
    }
}
