/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.junit.Before;
import org.junit.Test;

public class SelectionLayerAccessorTest {

    private final IUniqueIndexLayer uniqueIndexLayer = mock(IUniqueIndexLayer.class);
    private SelectionLayer selectionLayer;
    private SelectionLayerAccessor sla;
    private IRowDataProvider<Object> dataProvider;

    @SuppressWarnings("unchecked")
    @Before
    public void prepareSelection() {
        when(uniqueIndexLayer.getColumnCount()).thenReturn(5);
        when(uniqueIndexLayer.getRowCount()).thenReturn(5);
        dataProvider = mock(IRowDataProvider.class);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                final ILayerCell cell = mock(ILayerCell.class);
                when(cell.getOriginColumnPosition()).thenReturn(i);
                when(cell.getOriginRowPosition()).thenReturn(j);
                when(cell.getColumnPosition()).thenReturn(i);
                when(cell.getRowPosition()).thenReturn(j);
                when(cell.getColumnSpan()).thenReturn(1);
                when(cell.getRowSpan()).thenReturn(1);
                when(uniqueIndexLayer.getDataValueByPosition(i, j)).thenReturn(i + " " + j);
                when(dataProvider.getDataValue(i, j)).thenReturn(i + " " + j);
                when(uniqueIndexLayer.getCellByPosition(i, j)).thenReturn(cell);
            }
        }
        selectionLayer = new SelectionLayer(uniqueIndexLayer);
        sla = new SelectionLayerAccessor(dataProvider, selectionLayer, null);
    }

    @Test
    public void testGetLastSelectedRowPosition() {
        final IUniqueIndexLayer uniqueIndexLayer = mock(IUniqueIndexLayer.class);
        final SelectionLayer selectionLayer = new SelectionLayer(uniqueIndexLayer);
        final SelectionLayerAccessor sla = new SelectionLayerAccessor(null, selectionLayer, null);
        selectionLayer.setLastSelectedCell(5, 6);
        assertThat(sla.getLastSelectedRowPosition()).isEqualTo(6);
    }

    @Test
    public void testGetColumnCount() {
        assertThat(sla.getColumnCount()).isEqualTo(5);
    }

    @Test
    public void testGetSelectedPositions() {
        // no selection
        assertThat(sla.getSelectedPositions()).isEmpty();

        // selected region
        selectionLayer.selectRegion(1, 1, 2, 2);
        final PositionCoordinate[] coordinates = sla.getSelectedPositions();
        assertThat(coordinates).hasSize(4);
        assertThat(coordinates[0].getColumnPosition()).isEqualTo(1);
        assertThat(coordinates[0].getRowPosition()).isEqualTo(1);
        assertThat(coordinates[1].getColumnPosition()).isEqualTo(1);
        assertThat(coordinates[1].getRowPosition()).isEqualTo(2);
        assertThat(coordinates[2].getColumnPosition()).isEqualTo(2);
        assertThat(coordinates[2].getRowPosition()).isEqualTo(1);
        assertThat(coordinates[3].getColumnPosition()).isEqualTo(2);
        assertThat(coordinates[3].getRowPosition()).isEqualTo(2);
    }

    @Test
    public void testOnlyFullRowsAreSelected() {
        // no selection
        assertThat(sla.onlyFullRowsAreSelected()).isFalse();

        // selected full rows
        selectionLayer.selectRegion(0, 0, 5, 5);
        assertThat(sla.onlyFullRowsAreSelected()).isTrue();

        // first cells not selected
        selectionLayer.clear();
        selectionLayer.selectRegion(1, 1, 4, 4);
        assertThat(sla.onlyFullRowsAreSelected()).isFalse();

        // last cells not selected
        selectionLayer.clear();
        selectionLayer.selectRegion(0, 0, 4, 4);
        assertThat(sla.onlyFullRowsAreSelected()).isFalse();

        // some rows fully selected
        selectionLayer.clear();
        selectionLayer.selectRegion(0, 0, 4, 4);
        selectionLayer.selectRegion(0, 4, 5, 1);
        assertThat(sla.noFullRowIsSelected()).isFalse();
    }

    @Test
    public void testNoFullRowIsSelected() {
        // no selection
        assertThat(sla.noFullRowIsSelected()).isTrue();

        // selected full rows
        selectionLayer.selectRegion(0, 0, 5, 5);
        assertThat(sla.noFullRowIsSelected()).isFalse();

        // first cells not selected
        selectionLayer.clear();
        selectionLayer.selectRegion(1, 1, 4, 4);
        assertThat(sla.noFullRowIsSelected()).isTrue();

        // last cells not selected
        selectionLayer.clear();
        selectionLayer.selectRegion(0, 0, 4, 4);
        assertThat(sla.noFullRowIsSelected()).isTrue();

        // some rows fully selected
        selectionLayer.clear();
        selectionLayer.selectRegion(0, 0, 4, 4);
        selectionLayer.selectRegion(0, 4, 5, 1);
        assertThat(sla.noFullRowIsSelected()).isFalse();
    }

    @Test
    public void testGetElementSelectedAt() {
        // no selection
        assertThat(sla.getElementSelectedAt(2)).isNull();

        // selected row
        when(dataProvider.getRowObject(2)).thenReturn(new Object());
        selectionLayer.selectRegion(0, 2, 5, 1);
        assertThat(sla.getElementSelectedAt(2)).isNotNull();
    }

    @Test
    public void testExpandSelectionToWholeRows() {
        // no selection
        sla.expandSelectionToWholeRows();
        assertThat(sla.getSelectedPositions()).isEmpty();

        // one cell selected
        selectionLayer.selectRegion(2, 2, 1, 1);
        sla.expandSelectionToWholeRows();
        assertThat(sla.onlyFullRowsAreSelected()).isTrue();
    }

    @Test
    public void testFindNextSelectedElementRowIndex() {
        // no selection
        assertThat(sla.findNextSelectedElementRowIndex(-1)).isEqualTo(-1);

        // something selected
        selectionLayer.selectRegion(2, 2, 2, 2);
        assertThat(sla.findNextSelectedElementRowIndex(-1)).isEqualTo(2);
        assertThat(sla.findNextSelectedElementRowIndex(2)).isEqualTo(3);
        assertThat(sla.findNextSelectedElementRowIndex(10)).isEqualTo(10);
    }

    @Test
    public void testFindSelectedColumnsIndexesByRowIndex() {
        // no selection
        assertThat(sla.findSelectedColumnsIndexesByRowIndex(2)).isEmpty();

        // something selected
        selectionLayer.selectRegion(2, 2, 2, 2);
        assertThat(sla.findSelectedColumnsIndexesByRowIndex(1)).isEmpty();
        assertThat(sla.findSelectedColumnsIndexesByRowIndex(2)).containsExactly(2, 3);
        assertThat(sla.findSelectedColumnsIndexesByRowIndex(5)).isEmpty();
    }

    @Test
    public void testIsAnyCellSelectedInColumn() {
        // no selection
        assertThat(sla.isAnyCellSelectedInColumn(2)).isFalse();

        // something selected
        selectionLayer.selectRegion(2, 2, 2, 1);
        assertThat(sla.isAnyCellSelectedInColumn(0)).isFalse();
        assertThat(sla.isAnyCellSelectedInColumn(2)).isTrue();
        assertThat(sla.isAnyCellSelectedInColumn(4)).isFalse();
    }

    @Test
    public void testSelectCellContaining() {
        selectionLayer.selectRegion(2, 2, 2, 2);
        sla.selectCellContaining("2 3");
        assertThat(sla.getSelectedPositions()).hasSize(1);
        assertThat(sla.getSelectedPositions()[0].getColumnPosition()).isEqualTo(2);
        assertThat(sla.getSelectedPositions()[0].getRowPosition()).isEqualTo(3);
    }

    @Test
    public void testGetLabelFromCell() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                assertThat(sla.getLabelFromCell(j, i)).isEqualTo(i + " " + j);
            }
        }
    }
}
