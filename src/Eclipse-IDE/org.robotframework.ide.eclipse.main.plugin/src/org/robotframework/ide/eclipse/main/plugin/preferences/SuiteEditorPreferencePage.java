/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.SeparatorsMode;
import org.robotframework.red.jface.preferences.RegexValidatedStringFieldEditor;

public class SuiteEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private RadioGroupFieldEditor editors;

    private StringFieldEditor separatorEditor;

    private Group generalGroup;

    public SuiteEditorPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, RedPlugin.PLUGIN_ID));
    }

    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        createGeneralSettingsGroup(parent);
        new Label(parent, SWT.NONE);
        createTablesSettingsGroup(parent);
    }

    private void createGeneralSettingsGroup(final Composite parent) {
        generalGroup = new Group(parent, SWT.NONE);
        generalGroup.setText("General settings");
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(generalGroup);
        GridLayoutFactory.fillDefaults().applyTo(generalGroup);
        
        editors = new RadioGroupFieldEditor(RedPreferences.SEPARATOR_MODE,
                "When Tab key is pressed in source editor", 1, createLabelsAndValues(), generalGroup);
        addField(editors);
        final Label label = editors.getLabelControl(generalGroup);
        GridDataFactory.fillDefaults().indent(5, 10).applyTo(label);
        
        final String regex = "^(ss+)|t+|((s|t)+\\|(s|t)+)$";
        separatorEditor = new RegexValidatedStringFieldEditor(RedPreferences.SEPARATOR_TO_USE,
                "user defined separator (use '|', 's' for space or 't' for tab)", regex, generalGroup);
        separatorEditor.setErrorMessage(
                "User defined spearator should have at least one tab or two spaces, or bar '|' sourrounded "
                        + "with at least one space or tab");
        addField(separatorEditor);
        final SeparatorsMode currentMode = SeparatorsMode
                .valueOf(getPreferenceStore().getString(RedPreferences.SEPARATOR_MODE));
        separatorEditor.setEnabled(currentMode != SeparatorsMode.ALWAYS_TABS, generalGroup);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (event.getSource() == editors) {
            final SeparatorsMode newMode = SeparatorsMode.valueOf((String) event.getNewValue());
            separatorEditor.setEnabled(newMode != SeparatorsMode.ALWAYS_TABS, generalGroup);
        }
        super.propertyChange(event);
    }

    private String[][] createLabelsAndValues() {
        return new String[][] {
                new String[] { "the tab character ('\\t') should be used", SeparatorsMode.ALWAYS_TABS.name() },
                new String[] { "user defined separator should be used",
                        SeparatorsMode.ALWAYS_USER_DEFINED_SEPARATOR.name() },
                new String[] {
                        "file dependent seperator should be used ('\\t' for *.tsv files, user defined for *.robot)",
                        SeparatorsMode.FILETYPE_DEPENDENT.name() } };
    }

    private void createTablesSettingsGroup(final Composite parent) {
        final Group tablesGroup = new Group(parent, SWT.NONE);
        tablesGroup.setText("Tables");
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(tablesGroup);
        GridLayoutFactory.fillDefaults().applyTo(tablesGroup);

        final IntegerFieldEditor editor = new IntegerFieldEditor(RedPreferences.MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS,
                "Default number of columns for arguments in table editors", tablesGroup, 2);
        editor.setValidRange(1, 20);
        addField(editor);
    }

}
