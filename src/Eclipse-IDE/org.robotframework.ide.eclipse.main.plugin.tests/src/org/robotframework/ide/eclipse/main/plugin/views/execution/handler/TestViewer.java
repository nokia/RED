/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.red.viewers.TreeContentProvider;

/**
 * @author Michal Anglart
 *
 */
class TestViewer {

    static TreeViewer create(final Composite parent) {
        final TreeViewer viewer = new TreeViewer(parent);
        viewer.getTree().setHeaderVisible(false);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTree());
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(viewer.getTree());

        viewer.setContentProvider(new TestContentProvider());
        ViewerColumnsFactory.newColumn("")
                .withWidth(300)
                .shouldGrabAllTheSpaceLeft(true)
                .labelsProvidedBy(new TestLabelProvider())
                .createFor(viewer);

        return viewer;
    }

    static class Node {

        private Node parent;

        private final List<Node> children;

        private final String label;

        Node(final Node parent, final String label, final Node... children) {
            this.parent = parent;
            this.children = newArrayList(children);
            this.label = label;

            for (final Node child : children) {
                child.parent = this;
            }
        }
    }

    private static class TestContentProvider extends TreeContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            return ((List<?>) inputElement).toArray();
        }

        @Override
        public Object[] getChildren(final Object element) {
            return ((Node) element).children.toArray();
        }

        @Override
        public Object getParent(final Object element) {
            return ((Node) element).parent;
        }

        @Override
        public boolean hasChildren(final Object element) {
            return getChildren(element).length > 0;
        }
    }

    private static class TestLabelProvider extends ColumnLabelProvider {

        @Override
        public String getText(final Object element) {
            return ((Node) element).label;
        }
    }
}
