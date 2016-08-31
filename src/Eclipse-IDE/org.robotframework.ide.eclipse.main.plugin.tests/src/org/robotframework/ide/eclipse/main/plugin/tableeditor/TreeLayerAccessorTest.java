package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static com.google.common.collect.Lists.newArrayList;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

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
        List<Integer> expandedRowIndexes = newArrayList(0, 8);
        int lastSelectedRowPosition = 7;
        int rowCountChange = 1;

        treeLayerAccessor.collapseRowsAfterRowCountChange(expandedRowIndexes, lastSelectedRowPosition, rowCountChange);

        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(0));
        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(1) + rowCountChange);
    }
    
    @Test
    public void testCollapseRowsAfterRowCountChange_whenOneKeywordDefAddedBetweenExpandedIndexes() {
        List<Integer> expandedRowIndexes = newArrayList(0, 4, 8);
        int lastSelectedRowPosition = 4;
        int rowCountChange = 2;

        treeLayerAccessor.collapseRowsAfterRowCountChange(expandedRowIndexes, lastSelectedRowPosition, rowCountChange);

        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(0));
        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(1) + rowCountChange);
        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(2) + rowCountChange);
    }
    
    @Test
    public void testCollapseRowsAfterRowCountChange_whenOneRowRemovedBetweenExpandedIndexes() {
        List<Integer> expandedRowIndexes = newArrayList(0, 8);
        int lastSelectedRowPosition = 5;
        int rowCountChange = -1;

        treeLayerAccessor.collapseRowsAfterRowCountChange(expandedRowIndexes, lastSelectedRowPosition, rowCountChange);

        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(0));
        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(1) + rowCountChange);
    }
    
    @Test
    public void testCollapseRowsAfterRowCountChange_whenOneKeywordDefRemovedBetweenExpandedIndexes() {
        List<Integer> expandedRowIndexes = newArrayList(0, 8);
        int lastSelectedRowPosition = 4;
        int rowCountChange = -4;

        treeLayerAccessor.collapseRowsAfterRowCountChange(expandedRowIndexes, lastSelectedRowPosition, rowCountChange);

        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(0));
        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(1) + rowCountChange);
    }
    
    @Test
    public void testCollapseRowsAfterRowCountChange_whenFirstKeywordDefRemovedBeforeExpandedIndexes() {
        List<Integer> expandedRowIndexes = newArrayList(0, 6, 10);
        int lastSelectedRowPosition = 0;
        int rowCountChange = -6;

        treeLayerAccessor.collapseRowsAfterRowCountChange(expandedRowIndexes, lastSelectedRowPosition, rowCountChange);
        
        verify(treeLayer, times(2)).collapseTreeRow(anyInt());
        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(1) + rowCountChange);
        verify(treeLayer).collapseTreeRow(expandedRowIndexes.get(2) + rowCountChange);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testExpandCollapsedRowsBeforeRowCountChange() {
        ITreeRowModel treeModel = mock(ITreeRowModel.class);
        when(treeLayer.getModel()).thenReturn(treeModel);
        when(treeModel.isCollapsed(0)).thenReturn(true);
        when(treeModel.isCollapsed(1)).thenReturn(false);
        when(treeModel.isCollapsed(2)).thenReturn(false);
        when(treeModel.isCollapsed(3)).thenReturn(true);

        int rowCount = 4;
        List<Integer> expandedRowsIndexes = treeLayerAccessor.expandCollapsedRowsBeforeRowCountChange(rowCount);

        assertTrue(expandedRowsIndexes.size() == 2);
        assertTrue(expandedRowsIndexes.contains(0));
        assertTrue(expandedRowsIndexes.contains(3));
        verify(treeLayer).expandTreeRow(0);
        verify(treeLayer).expandTreeRow(3);
    }
}
