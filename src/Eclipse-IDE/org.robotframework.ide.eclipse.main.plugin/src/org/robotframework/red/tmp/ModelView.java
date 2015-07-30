package org.robotframework.red.tmp;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;

public class ModelView {

    @PostConstruct
    public void postConstruct(final Composite parent) {

        final FillLayout layout = new FillLayout();
        layout.type = SWT.VERTICAL;
        layout.marginHeight = 2;
        layout.marginWidth = 2;
        parent.setLayout(layout);
        
        final Label label = new Label(parent, SWT.NONE);
        label.setText("Images: 0 Fonts: 0 Colors: 0");

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
                            label.setText("Images: " + ImagesManager.size() + " Fonts: " + FontsManager.size()
                                    + " Colors: " + ColorsManager.size());

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
}
