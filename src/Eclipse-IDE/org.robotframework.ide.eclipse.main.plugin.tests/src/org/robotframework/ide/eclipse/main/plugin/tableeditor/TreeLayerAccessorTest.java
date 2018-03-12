/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.junit.Before;
import org.junit.Test;

public class TreeLayerAccessorTest {

    private TreeLayerAccessor treeLayerAccessor;

    private TreeLayer treeLayer;

    @Before
    public void setup() {
        treeLayer = mock(TreeLayer.class);
        treeLayerAccessor = new TreeLayerAccessor(treeLayer);
    }

    @Test
    public void testCollapseRowsAfterRowCountChange_whenOneRowAddedBetweenExpandedIndexes() {
        final List<Integer> expandedRowIndexes = newArrayList(0, 8);
        final int lastSelectedRowPosition = 7;
        final int rowCountChange = 1;

        treeLayerAccessor.collapseRowsAfterRowCountChange(expandedRowIndexes, lastSelectedRowPosition, rowCountChange);

        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(0));
        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(1) + rowCountChange);
    }
    
    @Test
    public void testCollapseRowsAfterRowCountChange_whenOneKeywordDefAddedBetweenExpandedIndexes() {
        final List<Integer> expandedRowIndexes = newArrayList(0, 4, 8);
        final int lastSelectedRowPosition = 4;
        final int rowCountChange = 2;

        treeLayerAccessor.collapseRowsAfterRowCountChange(expandedRowIndexes, lastSelectedRowPosition, rowCountChange);

        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(0));
        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(1) + rowCountChange);
        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(2) + rowCountChange);
    }
    
    @Test
    public void testCollapseRowsAfterRowCountChange_whenOneRowRemovedBetweenExpandedIndexes() {
        final List<Integer> expandedRowIndexes = newArrayList(0, 8);
        final int lastSelectedRowPosition = 5;
        final int rowCountChange = -1;

        treeLayerAccessor.collapseRowsAfterRowCountChange(expandedRowIndexes, lastSelectedRowPosition, rowCountChange);

        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(0));
        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(1) + rowCountChange);
    }
    
    @Test
    public void testCollapseRowsAfterRowCountChange_whenOneKeywordDefRemovedBetweenExpandedIndexes() {
        final List<Integer> expandedRowIndexes = newArrayList(0, 8);
        final int lastSelectedRowPosition = 4;
        final int rowCountChange = -4;

        treeLayerAccessor.collapseRowsAfterRowCountChange(expandedRowIndexes, lastSelectedRowPosition, rowCountChange);

        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(0));
        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(1) + rowCountChange);
    }
    
    @Test
    public void testCollapseRowsAfterRowCountChange_whenFirstKeywordDefRemovedBeforeExpandedIndexes() {
        final List<Integer> expandedRowIndexes = newArrayList(0, 6, 10);
        final int lastSelectedRowPosition = 0;
        final int rowCountChange = -6;

        treeLayerAccessor.collapseRowsAfterRowCountChange(expandedRowIndexes, lastSelectedRowPosition, rowCountChange);
        
        verify(treeLayer, times(2)).collapseTreeRow(anyInt());
        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(1) + rowCountChange);
        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(2) + rowCountChange);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testExpandCollapsedRowsBeforeRowCountChange() {
        final ITreeRowModel treeModel = mock(ITreeRowModel.class);
        when(treeLayer.getModel()).thenReturn(treeModel);
        when(treeModel.isCollapsed(0)).thenReturn(true);
        when(treeModel.isCollapsed(1)).thenReturn(false);
        when(treeModel.isCollapsed(2)).thenReturn(false);
        when(treeModel.isCollapsed(3)).thenReturn(true);

        final int rowCount = 4;
        final List<Integer> expandedRowsIndexes = treeLayerAccessor.expandCollapsedRowsBeforeRowCountChange(rowCount);

        assertTrue(expandedRowsIndexes.size() == 2);
        assertTrue(expandedRowsIndexes.contains(0));
        assertTrue(expandedRowsIndexes.contains(3));
        verify(treeLayer).expandTreeRow(0);
        verify(treeLayer).expandTreeRow(3);
    }
}
