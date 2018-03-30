/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

class AdditionalArgumentsComposite extends Composite {

    private Text argumentsText;

    AdditionalArgumentsComposite(final Composite parent, final ModifyListener listener) {
        super(parent, SWT.NONE);

        GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 5).applyTo(this);

        createAdditionalArgumentsText(listener);
        BrowseButtons.selectVariableButton(this, argumentsText);
    }

    private void createAdditionalArgumentsText(final ModifyListener listener) {
        argumentsText = new Text(this, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(argumentsText);
        argumentsText.addModifyListener(listener);
    }

    void setInput(final String arguments) {
        argumentsText.setText(arguments);
    }

    String getArguments() {
        return argumentsText.getText().trim();
    }

}
