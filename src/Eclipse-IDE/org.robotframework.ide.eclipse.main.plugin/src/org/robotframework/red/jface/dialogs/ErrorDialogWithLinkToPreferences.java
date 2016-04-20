/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.dialogs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class ErrorDialogWithLinkToPreferences extends MessageDialog {

    private String preferenceId;

    private String preferenceName;

    public ErrorDialogWithLinkToPreferences(final Shell parentShell, final String dialogTitle,
            final String dialogMessage, final String preferenceId, final String preferenceName) {
        super(parentShell, dialogTitle, null, dialogMessage, MessageDialog.ERROR, new String[] {}, 0);
        this.preferenceId = preferenceId;
        this.preferenceName = preferenceName;
    }

    @Override
    protected Control createCustomArea(final Composite parent) {
        final Link link = new Link(parent, SWT.NONE);
        GridDataFactory.fillDefaults().indent(0, 20).applyTo(link);
        link.setText("Go to preferences page: <a href=\"" + preferenceId + "\">'" + preferenceName + "'</a>");
        link.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(getShell(), e.text, null, null).open();
                close();
            }
        });
        return link;
    }
}
