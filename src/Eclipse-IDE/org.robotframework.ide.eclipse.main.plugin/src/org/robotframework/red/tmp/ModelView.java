/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.tmp;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class ModelView {

    @PostConstruct
    public void postConstruct(final Composite parent) {

        final FillLayout layout = new FillLayout();
        layout.type = SWT.VERTICAL;
        layout.marginHeight = 2;
        layout.marginWidth = 2;
        parent.setLayout(layout);
        
        final TreeViewer viewer = new TreeViewer(parent);
        viewer.getTree().setHeaderVisible(true);
        viewer.setContentProvider(new ModelContentProvider());

        final TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
        column.setLabelProvider(new ModelLabelProvider());
        column.getColumn().setText("Element");
        column.getColumn().setWidth(200);

        final TableViewer viewer2 = new TableViewer(parent);
        viewer2.getTable().setHeaderVisible(true);
        viewer2.setContentProvider(new ContextContentProvider());

        final TableViewerColumn column2 = new TableViewerColumn(viewer2, SWT.NONE);
        column2.setLabelProvider(new ContextLabelProvider());
        column2.getColumn().setText("Context");
        column2.getColumn().setWidth(200);

        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    parent.getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            viewer.setInput(RedPlugin.getModelManager().getModel());
                            viewer.expandAll();

                            viewer2.setInput(new Object());
                        }
                    });
                    try {
                        Thread.sleep(2000);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
            
    }

    @Focus
    public void onFocus() {

    }

    private static class ContextContentProvider implements IStructuredContentProvider {

        @Override
        public void dispose() {
            // nothing to do
        }

        @Override
        public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
            // nothing to do
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object[] getElements(final Object inputElement) {
            final IContextService service = PlatformUI.getWorkbench().getService(IContextService.class);
            return filter(service.getActiveContextIds()).toArray();
        }

        private Collection<String> filter(final Collection<String> activeContextIds) {
            return Collections2.filter(activeContextIds, new Predicate<String>() {
                @Override
                public boolean apply(final String contextId) {
                    return contextId.startsWith("org.robot");
                }
            });
        }
    }

    private static class ContextLabelProvider extends ColumnLabelProvider {

        @Override
        public String getText(final Object element) {
            return element.toString();
        }
    }

    private static class ModelContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
            // nothing to do
        }

        @Override
        public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
            // nothing to do
        }

        @Override
        public Object[] getElements(final Object inputElement) {
            return ((RobotElement) inputElement).getChildren().toArray();
        }

        @Override
        public Object[] getChildren(final Object parentElement) {
            return ((RobotElement) parentElement).getChildren().toArray();
        }

        @Override
        public Object getParent(final Object element) {
            return ((RobotElement) element).getParent();
        }

        @Override
        public boolean hasChildren(final Object element) {
            return true;
        }
    }

    private class ModelLabelProvider extends ColumnLabelProvider {

        @Override
        public String getText(final Object element) {
            return ((RobotElement) element).getName();
        }
    }
}
