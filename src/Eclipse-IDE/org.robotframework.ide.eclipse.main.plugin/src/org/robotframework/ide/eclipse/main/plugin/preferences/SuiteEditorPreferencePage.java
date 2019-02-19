/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.CellCommitBehavior;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.CellWrappingStrategy;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.SeparatorsMode;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.ElementOpenMode;
import org.robotframework.red.jface.preferences.ComboBoxFieldEditor;
import org.robotframework.red.jface.preferences.RegexStringFieldEditor;
import org.robotframework.red.jface.preferences.RegexValidatedStringFieldEditor;

public class SuiteEditorPreferencePage extends RedFieldEditorPreferencePage {

    private static final String SEPARATOR_PATTERN = "^(ss+)|t+|((s|t)+\\|(s|t)+)$"; //$NON-NLS-1$

    private Consumer<String> separatorEnablementUpdater;

    private BiConsumer<Boolean, Boolean> variablesEnablementUpdater;

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        createLink(parent);
        createGeneralSettingsGroup(parent);
        createTablesSettingsGroup(parent);
        createSourceSettingsGroup(parent);
        createVariablesSettingsEditors(parent);
    }

    private void createLink(final Composite parent) {
        final Link link = new Link(parent, SWT.NONE);
        GridDataFactory.fillDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .hint(150, SWT.DEFAULT)
                .span(2, 1)
                .grab(true, false)
                .applyTo(link);

        final String generalTextEditorPageId = "org.eclipse.ui.preferencePages.GeneralTextEditor";
        final String colorsAndFontsPageId = "org.eclipse.ui.preferencePages.ColorsAndFonts";

        final String text = "Robot editor preferences. See <a href=\"" + generalTextEditorPageId
                + "\">'Text Editors'</a> for general text editor preferences " + "and <a href=\"" + colorsAndFontsPageId
                + "\">'Colors and Fonts'</a> to configure the font.";
        link.setText(text);
        link.addSelectionListener(widgetSelectedAdapter(e -> {
            if (generalTextEditorPageId.equals(e.text)) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
            } else if (colorsAndFontsPageId.equals(e.text)) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null,
                        "selectFont:org.robotframework.ide.textfont");
            }
        }));
    }

    private void createGeneralSettingsGroup(final Composite parent) {
        final Group generalGroup = new Group(parent, SWT.NONE);
        generalGroup.setText("General");
        GridDataFactory.fillDefaults().indent(0, 20).grab(true, false).span(2, 1).applyTo(generalGroup);
        GridLayoutFactory.fillDefaults().applyTo(generalGroup);

        final BooleanFieldEditor parentDirectoryInTabEditor = new BooleanFieldEditor(
                RedPreferences.PARENT_DIRECTORY_NAME_IN_TAB, "Add parent directory name to editor tab", generalGroup);
        addField(parentDirectoryInTabEditor);
        final Button parentDirectoryInTabCheckbox = (Button) parentDirectoryInTabEditor
                .getDescriptionControl(generalGroup);
        GridDataFactory.fillDefaults().indent(5, 10).span(2, 1).applyTo(parentDirectoryInTabCheckbox);

        final ComboBoxFieldEditor elementsOpeningStrategyEditor = new ComboBoxFieldEditor(
                RedPreferences.FILE_ELEMENTS_OPEN_MODE, "Prefer opening file elements from Project Explorer in",
                "File elements (like e.g. test case) will be opened in page chosen here unless last time editor "
                        + "was closed with different active page",
                5, createElementsOpenModeLabelsAndValues(), generalGroup);
        addField(elementsOpeningStrategyEditor);
    }

    private String[][] createElementsOpenModeLabelsAndValues() {
        return new String[][] { new String[] { "source page of editor", ElementOpenMode.OPEN_IN_SOURCE.name() },
                new String[] { "designated table page of editor", ElementOpenMode.OPEN_IN_TABLES.name() } };
    }

    private void createTablesSettingsGroup(final Composite parent) {
        final Group tablesGroup = new Group(parent, SWT.NONE);
        tablesGroup.setText("Tables");
        GridDataFactory.fillDefaults().indent(0, 20).grab(true, false).span(2, 1).applyTo(tablesGroup);
        GridLayoutFactory.fillDefaults().applyTo(tablesGroup);

        final IntegerFieldEditor columnsEditor = new IntegerFieldEditor(
                RedPreferences.MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS,
                "Default number of columns for arguments in table editors", tablesGroup, 2);
        columnsEditor.setValidRange(1, 20);
        addField(columnsEditor);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(columnsEditor.getLabelControl(tablesGroup));

        final ComboBoxFieldEditor longCellContentStrategy = new ComboBoxFieldEditor(RedPreferences.CELL_WRAPPING,
                "When cell is too small for content",
                "", 5, createLongCellContentLabelsAndValues(), tablesGroup);
        addField(longCellContentStrategy);

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

    private String[][] createLongCellContentLabelsAndValues() {
        return new String[][] {
            new String[] { "cut the content", CellWrappingStrategy.SINGLE_LINE_CUT.name() },
            new String[] { "wrap the content", CellWrappingStrategy.WRAP.name() } };
    }

    private void createSourceSettingsGroup(final Composite parent) {
        final Group sourceGroup = new Group(parent, SWT.NONE);
        sourceGroup.setText("Source");
        GridDataFactory.fillDefaults().indent(0, 20).grab(true, false).span(2, 1).applyTo(sourceGroup);
        GridLayoutFactory.fillDefaults().applyTo(sourceGroup);

        final Composite editorsParent = new Composite(sourceGroup, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(editorsParent);
        final RadioGroupFieldEditor editors = new RadioGroupFieldEditor(RedPreferences.SEPARATOR_MODE,
                "When Tab key is pressed in source editor", 1, createTabPressLabelsAndValues(), editorsParent);
        addField(editors);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(editors.getLabelControl(editorsParent));

        final Composite separatorParent = new Composite(sourceGroup, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(separatorParent);
        final RegexValidatedStringFieldEditor separatorEditor = new RegexValidatedStringFieldEditor(
                RedPreferences.SEPARATOR_TO_USE, "User defined separator (use '|', 's' for space or 't' for tab)",
                SEPARATOR_PATTERN, separatorParent);
        separatorEditor.setErrorMessage(
                "User defined separator should have at least one tab or two spaces, or bar '|' surrounded "
                        + "with at least one space or tab");
        addField(separatorEditor);
        GridDataFactory.fillDefaults().indent(25, 0).applyTo(separatorEditor.getLabelControl(separatorParent));

        final Composite sourceJumpModeParent = new Composite(sourceGroup, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(sourceJumpModeParent);
        final BooleanFieldEditor sourceJumpModeEditor = new BooleanFieldEditor(
                RedPreferences.SEPARATOR_JUMP_MODE_ENABLED, "Jump out of active region", sourceJumpModeParent);
        addField(sourceJumpModeEditor);
        final Button sourceJumpModeEditorCheckbox = (Button) sourceJumpModeEditor
                .getDescriptionControl(sourceJumpModeParent);
        GridDataFactory.fillDefaults().indent(5, 0).applyTo(sourceJumpModeEditorCheckbox);

        separatorEnablementUpdater = value -> separatorEditor
                .setEnabled(SeparatorsMode.valueOf(value) != SeparatorsMode.ALWAYS_TABS, separatorParent);
        separatorEnablementUpdater.accept(getPreferenceStore().getString(RedPreferences.SEPARATOR_MODE));
    }

    private String[][] createTabPressLabelsAndValues() {
        return new String[][] {
                new String[] { "the tab character ('\\t') should be used", SeparatorsMode.ALWAYS_TABS.name() },
                new String[] { "user defined separator should be used",
                        SeparatorsMode.ALWAYS_USER_DEFINED_SEPARATOR.name() },
                new String[] {
                        "file dependent separator should be used ('\\t' for *.tsv files, user defined for *.robot)",
                        SeparatorsMode.FILE_TYPE_DEPENDENT.name() } };
    }

    private void createVariablesSettingsEditors(final Composite parent) {
        final Group varGroup = new Group(parent, SWT.NONE);
        varGroup.setText("Variables");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 1).applyTo(varGroup);
        GridLayoutFactory.fillDefaults().applyTo(varGroup);

        final Label varDescription = new Label(varGroup, SWT.WRAP);
        varDescription.setText("When one of robot variable identificators ($, @, &&, %) is typed");
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(varDescription);

        final BooleanFieldEditor varInsertionEnabled = new BooleanFieldEditor(
                RedPreferences.VARIABLES_BRACKETS_INSERTION_ENABLED, "Automatically add variable brackets", varGroup);
        addField(varInsertionEnabled);
        final Button varInsertionEnabledButton = (Button) varInsertionEnabled.getDescriptionControl(varGroup);
        GridDataFactory.fillDefaults().indent(5, 0).applyTo(varInsertionEnabledButton);

        final BooleanFieldEditor varWrappingEnabled = new BooleanFieldEditor(
                RedPreferences.VARIABLES_BRACKETS_INSERTION_WRAPPING_ENABLED,
                "Automatically wrap selected text with variable brackets", varGroup);
        addField(varWrappingEnabled);
        final Button varWrappingEnabledButton = (Button) varWrappingEnabled.getDescriptionControl(varGroup);
        GridDataFactory.fillDefaults().indent(25, 0).applyTo(varWrappingEnabledButton);

        final RegexStringFieldEditor varWrappingPattern = new RegexStringFieldEditor(
                RedPreferences.VARIABLES_BRACKETS_INSERTION_WRAPPING_PATTERN, "Wrap selected text pattern", varGroup);
        varWrappingPattern.setEmptyStringAllowed(false);
        varWrappingPattern.setErrorMessage("Pattern cannot be empty");
        addField(varWrappingPattern);
        GridDataFactory.fillDefaults().indent(25, 0).applyTo(varWrappingPattern.getLabelControl(varGroup));

        variablesEnablementUpdater = (isInsertionEnabled, isWrappingEnabled) -> {
            varWrappingEnabled.setEnabled(isInsertionEnabled, varGroup);
            varWrappingPattern.setEnabled(
                    isInsertionEnabled && (isWrappingEnabled || varWrappingEnabled.getBooleanValue()), varGroup);
        };
        final boolean isInsertionEnabled = getPreferenceStore()
                .getBoolean(RedPreferences.VARIABLES_BRACKETS_INSERTION_ENABLED);
        final boolean isWrappingEnabled = getPreferenceStore()
                .getBoolean(RedPreferences.VARIABLES_BRACKETS_INSERTION_WRAPPING_ENABLED);
        variablesEnablementUpdater.accept(isInsertionEnabled, isWrappingEnabled);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (event.getSource() instanceof BooleanFieldEditor
                && ((BooleanFieldEditor) event.getSource()).getPreferenceName()
                        .equals(RedPreferences.VARIABLES_BRACKETS_INSERTION_ENABLED)) {
            variablesEnablementUpdater.accept((Boolean) event.getNewValue(), false);
        } else if (event.getSource() instanceof BooleanFieldEditor
                && ((BooleanFieldEditor) event.getSource()).getPreferenceName()
                        .equals(RedPreferences.VARIABLES_BRACKETS_INSERTION_WRAPPING_ENABLED)) {
            variablesEnablementUpdater.accept(true, (Boolean) event.getNewValue());
        } else if (event.getSource() instanceof RadioGroupFieldEditor
                && ((RadioGroupFieldEditor) event.getSource()).getPreferenceName()
                        .equals(RedPreferences.SEPARATOR_MODE)) {
            separatorEnablementUpdater.accept((String) event.getNewValue());
        }
        super.propertyChange(event);
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();

        final boolean isInsertionEnabled = getPreferenceStore()
                .getDefaultBoolean(RedPreferences.VARIABLES_BRACKETS_INSERTION_ENABLED);
        final boolean isWrappingEnabled = getPreferenceStore()
                .getDefaultBoolean(RedPreferences.VARIABLES_BRACKETS_INSERTION_WRAPPING_ENABLED);
        variablesEnablementUpdater.accept(isInsertionEnabled, isWrappingEnabled);
        separatorEnablementUpdater.accept(getPreferenceStore().getDefaultString(RedPreferences.SEPARATOR_MODE));
    }
}
