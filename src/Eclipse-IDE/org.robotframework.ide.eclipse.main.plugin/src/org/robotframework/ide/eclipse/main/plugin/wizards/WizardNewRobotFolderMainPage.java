/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewFolderMainPage;

import com.google.common.collect.Maps;

public class WizardNewRobotFolderMainPage extends WizardNewFolderMainPage {

    private Button initializationFileButton;

    private final Map<String, Button> extensionButtons;

    public WizardNewRobotFolderMainPage(final String pageName, final IStructuredSelection selection,
            final String firstExtension, final String... restExtensions) {
        super(pageName, selection);
        extensionButtons = Maps.<String, Button> newLinkedHashMap();
        extensionButtons.put(firstExtension, null);
        for (final String extension : restExtensions) {
            extensionButtons.put(extension, null);
        }
    }

    @Override
    protected void createAdvancedControls(final Composite parent) {
        initializationFileButton = new Button(parent, SWT.CHECK);
        initializationFileButton.setText("Create suite initialization file");


        boolean isFirst = true;
        for (final String extension : extensionButtons.keySet()) {
            final Button button = new Button(parent, SWT.RADIO);
            button.setText("as ." + extension + " file");
            button.setSelection(isFirst);
            button.setEnabled(false);
            GridDataFactory.fillDefaults().indent(20, 0).applyTo(button);

            extensionButtons.put(extension, button);

            isFirst = false;
        }

        initializationFileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                for (final Button button : extensionButtons.values()) {
                    button.setEnabled(initializationFileButton.getSelection());
                }
            }
        });
        super.createAdvancedControls(parent);
    }

    boolean shouldInitFileBeCreated() {
        return initializationFileButton.getSelection();
    }

    String getInitFileExtension() {
        for (final Entry<String, Button> entry : extensionButtons.entrySet()) {
            if (entry.getValue().getSelection()) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("There should be extension chosen!");
    }
}
