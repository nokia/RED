/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.ShellProvider;

public class RfLintValidationPreferencePageTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @AfterClass
    public static void afterSuite() {
        final IPreferenceStore store = RedPlugin.getDefault().getPreferenceStore();
        store.putValue(RedPreferences.RFLINT_RULES_FILES, "");
        store.putValue(RedPreferences.RFLINT_RULES_CONFIG_NAMES, "");
        store.putValue(RedPreferences.RFLINT_RULES_CONFIG_SEVERITIES, "");
        store.putValue(RedPreferences.RFLINT_RULES_CONFIG_ARGS, "");
        store.putValue(RedPreferences.RFLINT_ADDITIONAL_ARGUMENTS, "");
    }

    @Test
    public void initDoesNothing() {
        final IWorkbench workbench = mock(IWorkbench.class);

        final RfLintValidationPreferencePage page = new RfLintValidationPreferencePage();
        page.init(workbench);

        verifyZeroInteractions(workbench);
    }

    @Test
    public void twoTablesArePlacedAtThePage() {
        final RfLintValidationPreferencePage page = new RfLintValidationPreferencePage();
        page.createControl(shellProvider.getShell());

        final List<Table> tables = getTables();
        assertThat(tables).hasSize(2);
    }

    @Test
    public void oneTextFieldIsPlacedAtThePage() {
        final RfLintValidationPreferencePage page = new RfLintValidationPreferencePage();
        page.createControl(shellProvider.getShell());

        final List<Text> textFields = getTextFields();
        assertThat(textFields).hasSize(1);
    }

    @Test
    public void rulesFilesTableDisplaysAllTheRulesFiles() {
        final IPreferenceStore store = RedPlugin.getDefault().getPreferenceStore();
        store.putValue(RedPreferences.RFLINT_RULES_FILES, "/path/to/fst.py;/path/to/snd.py");

        final RfLintValidationPreferencePage page = new RfLintValidationPreferencePage();
        page.createControl(shellProvider.getShell());

        final Table table = getRulesFilesTable();
        assertThat(table.getItemCount()).isEqualTo(3);
        assertThat(table.getItem(0).getText()).isEqualTo("/path/to/fst.py");
        assertThat(table.getItem(1).getText()).isEqualTo("/path/to/snd.py");
        assertThat(table.getItem(2).getText()).isEqualTo("...add new rules file");
    }

    @Test
    public void rulesTableDisplaysAllTheRules() {
        final IPreferenceStore store = RedPlugin.getDefault().getPreferenceStore();
        store.putValue(RedPreferences.RFLINT_RULES_CONFIG_NAMES, "Rule1;Rule2");
        store.putValue(RedPreferences.RFLINT_RULES_CONFIG_SEVERITIES, "DEFAULT;ERROR");
        store.putValue(RedPreferences.RFLINT_RULES_CONFIG_ARGS, ";100");

        final RfLintValidationPreferencePage page = new RfLintValidationPreferencePage();
        page.createControl(shellProvider.getShell());

        final Table table = getRulesTable();
        assertThat(table.getItemCount()).isEqualTo(3);
        assertThat(table.getItem(0).getText(0)).isEqualTo("Rule1");
        assertThat(table.getItem(0).getText(1)).isEqualTo("default");
        assertThat(table.getItem(0).getText(2)).isEqualTo("");
        assertThat(table.getItem(1).getText(0)).isEqualTo("Rule2");
        assertThat(table.getItem(1).getText(1)).isEqualTo("Error");
        assertThat(table.getItem(1).getText(2)).isEqualTo("100");
        assertThat(table.getItem(2).getText(0)).isEqualTo("...add new rule");
        assertThat(table.getItem(2).getText(1)).isEqualTo("");
        assertThat(table.getItem(2).getText(2)).isEqualTo("");
    }

    @Test
    public void additionalArgumentsFieldDisplaysCustomArguments() {
        final IPreferenceStore store = RedPlugin.getDefault().getPreferenceStore();
        store.putValue(RedPreferences.RFLINT_ADDITIONAL_ARGUMENTS, "custom arguments");

        final RfLintValidationPreferencePage page = new RfLintValidationPreferencePage();
        page.createControl(shellProvider.getShell());

        assertThat(getAdditionalArgumentsField().getText()).isEqualTo("custom arguments");
    }

    @Test
    public void valuesAndPreferencesAreCorrectlyUpdated() {
        final IPreferenceStore store = RedPlugin.getDefault().getPreferenceStore();
        store.putValue(RedPreferences.RFLINT_RULES_FILES, "/path/to/fst.py;/path/to/snd.py");
        store.putValue(RedPreferences.RFLINT_RULES_CONFIG_NAMES, "Rule1;Rule2");
        store.putValue(RedPreferences.RFLINT_RULES_CONFIG_SEVERITIES, "DEFAULT;ERROR");
        store.putValue(RedPreferences.RFLINT_RULES_CONFIG_ARGS, ";100");
        store.putValue(RedPreferences.RFLINT_ADDITIONAL_ARGUMENTS, "custom arguments");

        final RfLintValidationPreferencePage page = new RfLintValidationPreferencePage();
        page.createControl(shellProvider.getShell());

        assertNotEmptyValues();
        assertNotEmptyPreferences();

        page.performDefaults();

        assertEmptyValues();
        assertNotEmptyPreferences();

        page.performOk();

        assertEmptyValues();
        assertEmptyPreferences();
    }

    private void assertNotEmptyValues() {
        assertThat(getRulesTable().getItemCount()).isGreaterThan(1);
        assertThat(getRulesFilesTable().getItemCount()).isGreaterThan(1);
        assertThat(getAdditionalArgumentsField().getText()).isNotEmpty();
    }

    private void assertEmptyValues() {
        assertThat(getRulesTable().getItemCount()).isEqualTo(1);
        assertThat(getRulesFilesTable().getItemCount()).isEqualTo(1);
        assertThat(getAdditionalArgumentsField().getText()).isEmpty();
    }

    private void assertNotEmptyPreferences() {
        assertThat(RedPlugin.getDefault().getPreferences().getRfLintRules()).isNotEmpty();
        assertThat(RedPlugin.getDefault().getPreferences().getRfLintRulesFiles()).isNotEmpty();
        assertThat(RedPlugin.getDefault().getPreferences().getRfLintAdditionalArguments()).isNotEmpty();
    }

    private void assertEmptyPreferences() {
        assertThat(RedPlugin.getDefault().getPreferences().getRfLintRules()).isEmpty();
        assertThat(RedPlugin.getDefault().getPreferences().getRfLintRulesFiles()).isEmpty();
        assertThat(RedPlugin.getDefault().getPreferences().getRfLintAdditionalArguments()).isEmpty();
    }

    private Table getRulesTable() {
        return getTables().get(0);
    }

    private Table getRulesFilesTable() {
        return getTables().get(1);
    }

    private List<Table> getTables() {
        return Stream.of(((Composite) shellProvider.getShell().getChildren()[0]).getChildren())
                .filter(c -> c instanceof Table)
                .map(c -> Table.class.cast(c))
                .collect(toList());
    }

    private Text getAdditionalArgumentsField() {
        return getTextFields().get(0);
    }

    private List<Text> getTextFields() {
        return Stream.of(((Composite) shellProvider.getShell().getChildren()[0]).getChildren())
                .filter(c -> c instanceof Composite)
                .map(c -> Composite.class.cast(c))
                .flatMap(c -> Stream.of(c.getChildren()))
                .filter(c -> c instanceof Text)
                .map(c -> Text.class.cast(c))
                .collect(toList());
    }
}
