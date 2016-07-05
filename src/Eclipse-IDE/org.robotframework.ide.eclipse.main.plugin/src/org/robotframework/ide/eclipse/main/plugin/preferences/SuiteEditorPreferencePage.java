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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.CellCommitBehavior;
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

        createLink(parent);
        createGeneralSettingsGroup(parent);
        new Label(parent, SWT.NONE);
        createTablesSettingsGroup(parent);
    }
    
    private void createLink(final Composite parent) {
        final Link link = new Link(parent, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).hint(150, SWT.DEFAULT).grab(true, false).applyTo(link);

        final String text = "Robot editor preferences. See <a href=\"org.eclipse.ui.preferencePages.GeneralTextEditor\">'Text Editors'</a> for general text editor preferences "
                + "and <a href=\"org.eclipse.ui.preferencePages.ColorsAndFonts\">'Colors and Fonts'</a> to configure the font.";
        link.setText(text);
        link.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if ("org.eclipse.ui.preferencePages.GeneralTextEditor".equals(e.text)) {
                    PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
                } else if ("org.eclipse.ui.preferencePages.ColorsAndFonts".equals(e.text)) {
                    PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null,
                            "selectFont:org.robotframework.ide.textfont");
                }
            }
        });
    }

    private void createGeneralSettingsGroup(final Composite parent) {
        generalGroup = new Group(parent, SWT.NONE);
        generalGroup.setText("General settings");
        GridDataFactory.fillDefaults().indent(0, 5).grab(true, false).span(2, 1).applyTo(generalGroup);
        GridLayoutFactory.fillDefaults().applyTo(generalGroup);
        
        editors = new RadioGroupFieldEditor(RedPreferences.SEPARATOR_MODE,
                "When Tab key is pressed in source editor", 1, createTabPressLabelsAndValues(), generalGroup);
        addField(editors);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(editors.getLabelControl(generalGroup));
        
        final String regex = "^(ss+)|t+|((s|t)+\\|(s|t)+)$";
        separatorEditor = new RegexValidatedStringFieldEditor(RedPreferences.SEPARATOR_TO_USE,
                "user defined separator (use '|', 's' for space or 't' for tab)", regex, generalGroup);
        separatorEditor.setErrorMessage(
                "User defined spearator should have at least one tab or two spaces, or bar '|' sourrounded "
                        + "with at least one space or tab");
        addField(separatorEditor);
        GridDataFactory.fillDefaults().indent(5, 0).applyTo(separatorEditor.getLabelControl(generalGroup));
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

    private String[][] createTabPressLabelsAndValues() {
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

        final IntegerFieldEditor columnsEditor = new IntegerFieldEditor(
                RedPreferences.MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS,
                "Default number of columns for arguments in table editors", tablesGroup, 2);
        columnsEditor.setValidRange(1, 20);
        addField(columnsEditor);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(columnsEditor.getLabelControl(tablesGroup));

        final ComboBoxFieldEditor behaviorOnCellCommitEditor = new ComboBoxFieldEditor(
                RedPreferences.BEHAVIOR_ON_CELL_COMMIT, "After pressing Enter in cell under edit", "", 5,
                createCellCommitLabelsAndValues(),
                tablesGroup);
        addField(behaviorOnCellCommitEditor);
    }

    private String[][] createCellCommitLabelsAndValues() {
        return new String[][] {
                new String[] { "stay in the same cell", CellCommitBehavior.STAY_IN_SAME_CELL.name() },
                new String[] { "move to next cell (previous with Shift pressed)",
                        CellCommitBehavior.MOVE_TO_ADJACENT_CELL.name() } };
    }

}
