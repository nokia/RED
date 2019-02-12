/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.jface.preferences.RegexStringFieldEditor;

public class VariablesPreferencePage extends RedFieldEditorPreferencePage {

    private Button insertionEnabledButton;

    private Button wrappingEnabledButton;

    private Text wrapPatternTextControl;

    public VariablesPreferencePage() {
        setDescription("Configure additional actions that should be performed when robot variables are typed");
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        createBracketsInsertionEditors(parent);
    }

    private void createBracketsInsertionEditors(final Composite parent) {
        final Group bracketsGroup = new Group(parent, SWT.NONE);
        bracketsGroup.setText("Variable brackets");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(bracketsGroup);
        GridLayoutFactory.fillDefaults().applyTo(bracketsGroup);
        final Label bracketsDescription = new Label(bracketsGroup, SWT.WRAP);
        bracketsDescription.setText("When one of robot variable identificators ($, @, &&) is typed");
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(bracketsDescription);

        final BooleanFieldEditor insertionEnabled = new BooleanFieldEditor(
                RedPreferences.VARIABLES_BRACKETS_INSERTION_ENABLED, "Automatically add variable brackets",
                bracketsGroup);
        insertionEnabled.load();
        addField(insertionEnabled);
        insertionEnabledButton = (Button) insertionEnabled.getDescriptionControl(bracketsGroup);
        GridDataFactory.fillDefaults().indent(5, 0).applyTo(insertionEnabledButton);
        final boolean isInsertionEnabled = RedPlugin.getDefault()
                .getPreferences()
                .isVariablesBracketsInsertionEnabled();

        final BooleanFieldEditor wrappingEnabled = new BooleanFieldEditor(
                RedPreferences.VARIABLES_BRACKETS_INSERTION_WRAPPING_ENABLED,
                "Automatically wrap selected text with variable brackets", bracketsGroup);
        wrappingEnabled.setEnabled(isInsertionEnabled, bracketsGroup);
        addField(wrappingEnabled);
        wrappingEnabledButton = (Button) wrappingEnabled.getDescriptionControl(bracketsGroup);
        GridDataFactory.fillDefaults().indent(5, 0).applyTo(wrappingEnabledButton);
        final boolean isWrappingEnabled = RedPlugin.getDefault()
                .getPreferences()
                .isVariablesBracketsInsertionWrappingEnabled();

        final RegexStringFieldEditor wrappingPattern = new RegexStringFieldEditor(
                RedPreferences.VARIABLES_BRACKETS_INSERTION_WRAPPING_PATTERN, "Wrap selected text pattern",
                bracketsGroup);
        wrappingPattern.setEnabled(isInsertionEnabled && isWrappingEnabled, bracketsGroup);
        wrappingPattern.setEmptyStringAllowed(false);
        wrappingPattern.setErrorMessage("Pattern cannot be empty");
        addField(wrappingPattern);
        wrapPatternTextControl = wrappingPattern.getTextControl(bracketsGroup);
        GridDataFactory.fillDefaults().indent(5, 0).span(2, 1).applyTo(wrappingPattern.getLabelControl(bracketsGroup));

        insertionEnabledButton.addSelectionListener(widgetSelectedAdapter(e -> {
            wrappingEnabled.setEnabled(insertionEnabledButton.getSelection(), bracketsGroup);
            wrappingPattern.setEnabled(insertionEnabledButton.getSelection() && wrappingEnabledButton.getSelection(),
                    bracketsGroup);
        }));
        wrappingEnabledButton.addSelectionListener(widgetSelectedAdapter(e -> {
            wrappingPattern.setEnabled(wrappingEnabledButton.getSelection(), bracketsGroup);
        }));
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();

        final boolean isInsertionEnabled = getPreferenceStore()
                .getDefaultBoolean(RedPreferences.VARIABLES_BRACKETS_INSERTION_ENABLED);
        final boolean isWrappingEnabled = getPreferenceStore()
                .getDefaultBoolean(RedPreferences.VARIABLES_BRACKETS_INSERTION_WRAPPING_ENABLED);
        wrappingEnabledButton.setEnabled(isWrappingEnabled);
        wrapPatternTextControl.setEnabled(isInsertionEnabled && isWrappingEnabled);
    }
}
