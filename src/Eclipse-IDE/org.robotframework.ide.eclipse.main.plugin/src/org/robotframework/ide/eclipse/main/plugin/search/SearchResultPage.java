/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;


/**
 * @author Michal Anglart
 */
public class SearchResultPage extends AbstractTextSearchViewPage {

    private TreeViewer viewer;

    public SearchResultPage() {
        super(FLAG_LAYOUT_TREE);
    }

    @Override
    protected void elementsChanged(final Object[] objects) {
        viewer.refresh();
    }

    @Override
    protected void clear() {
        viewer.setInput(null);
    }

    @Override
    protected void configureTreeViewer(final TreeViewer viewer) {
        this.viewer = viewer;
        viewer.setContentProvider(new SearchResultContentProvider());
        viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new SearchResultLabelProvider()));
    }

    @Override
    protected void configureTableViewer(final TableViewer viewer) {
        throw new IllegalStateException(
                "Robot Search Result page was configured to show tree. This method should not be called");
    }
}
