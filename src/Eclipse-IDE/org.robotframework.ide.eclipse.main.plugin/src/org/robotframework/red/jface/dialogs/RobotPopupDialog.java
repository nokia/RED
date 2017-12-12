/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.dialogs;

import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("restriction")
public abstract class RobotPopupDialog extends PopupDialog {

    private final IThemeEngine engine;

    public RobotPopupDialog(final Shell parent, final IThemeEngine engine) {
        super(parent, PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE | SWT.ON_TOP, true, true, true, false, false, null, null);
        this.engine = engine;
    }

    @Override
    protected Control createContents(final Composite parent) {
        parent.setLayout(new FillLayout(SWT.VERTICAL));
        return createDialogArea(parent);
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Control control = createDialogControls(parent);
        engine.applyStyles(control, true);
        return control;
    }

    protected abstract Control createDialogControls(Composite parent);

    @Override
    protected abstract Control getFocusControl();
}
