/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;

/**
 * @author mmarzec
 */
public class TreeLayerAccessor {

    private TreeLayer treeLayer;

    public TreeLayerAccessor(final TreeLayer treeLayer) {
        this.treeLayer = treeLayer;
    }

    public void expandAll() {
        treeLayer.expandAll();
    }

    public void collapseAll() {
        treeLayer.collapseAll();
    }

    public List<Integer> expandCollapsedRowsBeforeRowCountChange(final int currentRowCount) {
        final List<Integer> expandedRowsIndexes = new ArrayList<>();
        final ITreeRowModel<?> model = treeLayer.getModel();
        for (int i = 0; i < currentRowCount; i++) {
            if (model.isCollapsed(i)) {
                expandedRowsIndexes.add(i);
            }
        }
        for (final Integer rowIndex : expandedRowsIndexes) {
            treeLayer.expandTreeRow(rowIndex);
        }
        return expandedRowsIndexes;
    }
    
    public void collapseRowsAfterRowCountChange(final List<Integer> expandedRowIndexes,
            final int lastSelectedRowPosition, final int rowCountChange) {
        
        for (final Integer expandedRowIndex : expandedRowIndexes) {
            if (expandedRowIndex >= lastSelectedRowPosition) {
                if ((expandedRowIndex + rowCountChange) >= 0) {
                    treeLayer.collapseTreeRow(expandedRowIndex + rowCountChange);
                }
            } else {
                treeLayer.collapseTreeRow(expandedRowIndex);
            }
        }
    }

}
