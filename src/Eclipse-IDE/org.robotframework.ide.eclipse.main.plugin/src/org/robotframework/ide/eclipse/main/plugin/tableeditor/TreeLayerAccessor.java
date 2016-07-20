/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

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
}
