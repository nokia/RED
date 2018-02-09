/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import java.util.List;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.StructuredContentProvider;

/**
 * @author Michal Anglart
 *
 */
class HyperlinkDialog extends PopupDialog {

    static final String POPUP_TEXT = "Hyperlink dialog";
    private static final String TITLE = "Imported Resources and Libraries defining keywords which matches '%s'";
    private static final String TIP = " (double click to open)";

    private final String name;
    private final List<RedHyperlink> hyperlinks;
    private Control focusControl;


    HyperlinkDialog(final Shell parent, final String name, final List<RedHyperlink> hyperlinks) {
        super(parent, PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE | SWT.ON_TOP, true, true, true, false, false, null, null);
        this.name = name;
        this.hyperlinks = hyperlinks;
    }

    @Override
    protected Control createContents(final Composite parent) {
        parent.getShell().setText(POPUP_TEXT);
        parent.setLayout(new FillLayout(SWT.VERTICAL));
        return createDialogArea(parent);
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite insider = new Composite(parent, SWT.NONE);
        insider.setBackground(ColorsManager.getColor(SWT.COLOR_INFO_BACKGROUND));
        GridLayoutFactory.fillDefaults().applyTo(insider);

        final StyledText label = new StyledText(insider, SWT.WRAP);
        label.setBackground(ColorsManager.getColor(SWT.COLOR_INFO_BACKGROUND));
        final String title = String.format(TITLE, name);
        label.setText(title + TIP);
        label.setStyleRange(new StyleRange(0, title.length(), null, null, SWT.BOLD));

        GridDataFactory.fillDefaults().grab(true, false).indent(5, 5).applyTo(label);

        final TableViewer viewer = new TableViewer(insider, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.getTable().setHeaderVisible(false);
        viewer.getTable().setLinesVisible(false);
        viewer.getTable().setBackground(ColorsManager.getColor(SWT.COLOR_INFO_BACKGROUND));
        viewer.setContentProvider(new HyperlinksContentProvider());
        final IDoubleClickListener doubleClickListener = createDoubleClickListener();
        viewer.addDoubleClickListener(doubleClickListener);
        viewer.getTable().addDisposeListener(e -> viewer.removeDoubleClickListener(doubleClickListener));

        ViewerColumnsFactory.newColumn("")
                .labelsProvidedBy(new HyperlinksLabelProvider())
                .shouldGrabAllTheSpaceLeft(true)
                .withMinWidth(100)
                .createFor(viewer);

        viewer.setInput(hyperlinks);
        GridDataFactory.fillDefaults().grab(true, true).hint(300, 300).applyTo(viewer.getTable());

        focusControl = viewer.getTable();
        return insider;
    }

    private IDoubleClickListener createDoubleClickListener() {
        return new IDoubleClickListener() {

            @Override
            public void doubleClick(final DoubleClickEvent event) {
                final RedHyperlink hyperlinkToFollow = Selections.getSingleElement((IStructuredSelection)event.getSelection(), RedHyperlink.class);
                HyperlinkDialog.this.close();
                SwtThread.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        hyperlinkToFollow.open();
                    }
                });
            }
        };
    }

    @Override
    protected Control getFocusControl() {
        return focusControl;
    }

    private static class HyperlinksContentProvider extends StructuredContentProvider {
        @Override
        public Object[] getElements(final Object inputElement) {
            final List<?> hyperlinks = (List<?>) inputElement;
            return hyperlinks.toArray();
        }
    }

    private static class HyperlinksLabelProvider extends RedCommonLabelProvider {
        @Override
        public StyledString getStyledText(final Object element) {
            final RedHyperlink hyperlink = (RedHyperlink) element;
            final StyledString label = new StyledString(hyperlink.getLabelForCompoundHyperlinksDialog());
            final String additional = hyperlink.additionalLabelDecoration();
            if (!additional.isEmpty()) {
                label.append(" " + additional, Stylers.Common.ECLIPSE_DECORATION_STYLER);
            }
            return label;
        }

        @Override
        public Image getImage(final Object element) {
            final RedHyperlink hyperlink = (RedHyperlink) element;
            return ImagesManager.getImage(hyperlink.getImage());
        }
    }
}
