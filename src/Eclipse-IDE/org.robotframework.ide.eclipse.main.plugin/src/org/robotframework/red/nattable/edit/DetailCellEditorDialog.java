/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


/**
 * @author Michal Anglart
 *
 */
public class DetailCellEditorDialog extends PopupDialog {

    private Control composite;

    private final Rectangle initialBounds;

    private final DialogContentCreator contentCreator;

    public DetailCellEditorDialog(final Shell parent, final Rectangle initialBounds,
            final DialogContentCreator contentCreator) {
        super(parent, INFOPOPUP_SHELLSTYLE | INFOPOPUPRESIZE_SHELLSTYLE, false, false, false, false, false, "", "");
        this.initialBounds = initialBounds;
        this.contentCreator = contentCreator;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(initialBounds.width, initialBounds.height);
    }

    @Override
    protected Point getInitialLocation(final Point initialSize) {
        return new Point(initialBounds.x, initialBounds.y);
    }

    @Override
    protected Control createContents(final Composite parent) {
        parent.setLayout(new FillLayout(SWT.VERTICAL));
        return createDialogArea(parent);
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        composite = contentCreator.create(parent);
        return composite;
    }

    @Override
    protected Control getFocusControl() {
        return composite;
    }

    @Override
    public boolean close() {
        final DetailEntriesCollection<?> entries = ((DetailCellEditorEntriesComposite<?>) composite).getEntries();
        for (final DetailCellEditorEntry<?> entry : entries.getEntries()) {
            if (entry.isEditorOpened()) {
                return false;
            }
        }
        return super.close();
    }

    static interface DialogContentCreator {

        public Control create(final Composite parent);
    }
}
