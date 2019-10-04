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

        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);

        createAdditionalArgumentsText(listener);

        final Composite buttonsParent = new Composite(this, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(buttonsParent);
        GridDataFactory.fillDefaults().span(2, 1).align(SWT.END, SWT.FILL).applyTo(buttonsParent);
        BrowseButtons.selectVariableButton(buttonsParent, argumentsText::insert);
    }

    private void createAdditionalArgumentsText(final ModifyListener listener) {
        argumentsText = new Text(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        GridDataFactory.fillDefaults()
                .grab(true, false)
                .hint(SWT.DEFAULT, 60)
                .span(2, 1)
                .align(SWT.FILL, SWT.CENTER)
                .applyTo(argumentsText);
        argumentsText.addModifyListener(listener);
    }

    void setInput(final String arguments) {
        argumentsText.setText(arguments);
    }

    String getArguments() {
        return argumentsText.getText().trim();
    }

}
