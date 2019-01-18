/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.ShellProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ActiveStringSubstitutionSetsPreferencePageTest {

    private static final IStringVariableManager VARIABLE_MANAGER = VariablesPlugin.getDefault()
            .getStringVariableManager();

    private static final IValueVariable[] CUSTOM_VARIABLES = new IValueVariable[] {
            VARIABLE_MANAGER.newValueVariable("a", "", false, "0"),
            VARIABLE_MANAGER.newValueVariable("b", "", false, "0"),
            VARIABLE_MANAGER.newValueVariable("c", "", false, "0") };

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @BeforeClass
    public static void beforeSuite() throws CoreException {
        VARIABLE_MANAGER.addVariables(CUSTOM_VARIABLES);
    }

    @AfterClass
    public static void afterSuite() {
        VARIABLE_MANAGER.removeVariables(CUSTOM_VARIABLES);

        final IPreferenceStore store = RedPlugin.getDefault().getPreferenceStore();
        store.putValue(RedPreferences.STRING_VARIABLES_ACTIVE_SET, "");
        store.putValue(RedPreferences.STRING_VARIABLES_SETS, "");
    }

    @Test
    public void initDoesNothing() {
        final IWorkbench workbench = mock(IWorkbench.class);

        final ActiveStringSubstitutionSetsPreferencePage page = new ActiveStringSubstitutionSetsPreferencePage();
        page.init(workbench);

        verifyZeroInteractions(workbench);
    }

    @Test
    public void singleTreeIsPlacedAtThePage() {
        final ActiveStringSubstitutionSetsPreferencePage page = new ActiveStringSubstitutionSetsPreferencePage();
        page.createControl(shellProvider.getShell());

        final List<Tree> trees = getTrees();
        assertThat(trees).hasSize(1);
    }

    @Test
    public void variablesSetsTreeDisplaysAllTheSets() throws JsonProcessingException {
        final Map<String, List<List<String>>> input = new LinkedHashMap<>();
        input.put("set 1", newArrayList());
        input.get("set 1").add(newArrayList("a", "1"));
        input.get("set 1").add(newArrayList("b", "2"));
        input.get("set 1").add(newArrayList("c", "3"));
        input.put("set 2", newArrayList());
        input.get("set 2").add(newArrayList("a", "4"));
        input.get("set 2").add(newArrayList("c", "6"));

        final IPreferenceStore store = RedPlugin.getDefault().getPreferenceStore();
        store.putValue(RedPreferences.STRING_VARIABLES_SETS, new ObjectMapper().writeValueAsString(input));
        store.putValue(RedPreferences.STRING_VARIABLES_ACTIVE_SET, "set 2");

        final ActiveStringSubstitutionSetsPreferencePage page = new ActiveStringSubstitutionSetsPreferencePage();
        page.createControl(shellProvider.getShell());

        final Tree tree = getSetsTree();
        assertThat(tree.getItemCount()).isEqualTo(3);
        assertThat(tree.getItem(0).getText()).isEqualTo("set 1");
        assertThat(tree.getItem(0).getExpanded()).isTrue();
        assertThat(tree.getItem(0).getItems()).hasSize(3);
        assertThat(tree.getItem(0).getItems()[0].getText(0)).isEqualTo("a");
        assertThat(tree.getItem(0).getItems()[0].getText(1)).isEqualTo("1");
        assertThat(tree.getItem(0).getItems()[1].getText(0)).isEqualTo("b");
        assertThat(tree.getItem(0).getItems()[1].getText(1)).isEqualTo("2");
        assertThat(tree.getItem(0).getItems()[2].getText(0)).isEqualTo("c");
        assertThat(tree.getItem(0).getItems()[2].getText(1)).isEqualTo("3");

        assertThat(tree.getItem(1).getText()).isEqualTo("[active] set 2");
        assertThat(tree.getItem(1).getExpanded()).isTrue();
        assertThat(tree.getItem(1).getItems()).hasSize(3);
        assertThat(tree.getItem(1).getItems()[0].getText(0)).isEqualTo("a");
        assertThat(tree.getItem(1).getItems()[0].getText(1)).isEqualTo("4");
        assertThat(tree.getItem(1).getItems()[1].getText(0)).isEqualTo("b");
        assertThat(tree.getItem(1).getItems()[1].getText(1)).isEqualTo("0");
        assertThat(tree.getItem(1).getItems()[2].getText(0)).isEqualTo("c");
        assertThat(tree.getItem(1).getItems()[2].getText(1)).isEqualTo("6");

        assertThat(tree.getItem(2).getText()).isEqualTo("...add new variables set");
        assertThat(tree.getItem(2).getExpanded()).isFalse();
    }

    @Test
    public void valuesAndPreferencesAreCorrectlyUpdated() throws JsonProcessingException {
        final Map<String, List<List<String>>> input = new LinkedHashMap<>();
        input.put("set 1", newArrayList());
        input.get("set 1").add(newArrayList("a", "1"));
        input.get("set 1").add(newArrayList("b", "2"));
        input.get("set 1").add(newArrayList("c", "3"));
        input.put("set 2", newArrayList());
        input.get("set 2").add(newArrayList("a", "4"));
        input.get("set 2").add(newArrayList("c", "6"));

        final IPreferenceStore store = RedPlugin.getDefault().getPreferenceStore();
        store.putValue(RedPreferences.STRING_VARIABLES_SETS, new ObjectMapper().writeValueAsString(input));
        store.putValue(RedPreferences.STRING_VARIABLES_ACTIVE_SET, "set 2");

        final ActiveStringSubstitutionSetsPreferencePage page = new ActiveStringSubstitutionSetsPreferencePage();
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
        assertThat(getSetsTree().getItemCount()).isGreaterThan(1);
    }

    private void assertEmptyValues() {
        assertThat(getSetsTree().getItemCount()).isEqualTo(1);
    }

    private void assertNotEmptyPreferences() {
        assertThat(RedPlugin.getDefault().getPreferences().getOverriddenVariablesSets()).isNotEmpty();
        assertThat(RedPlugin.getDefault().getPreferences().getActiveVariablesSet()).isNotEmpty();
    }

    private void assertEmptyPreferences() {
        assertThat(RedPlugin.getDefault().getPreferences().getOverriddenVariablesSets()).isEmpty();
        assertThat(RedPlugin.getDefault().getPreferences().getActiveVariablesSet()).isEmpty();
    }

    private Tree getSetsTree() {
        return getTrees().get(0);
    }

    private List<Tree> getTrees() {
        return Stream.of(((Composite) shellProvider.getShell().getChildren()[0]).getChildren())
                .filter(c -> c instanceof Tree)
                .map(c -> Tree.class.cast(c))
                .collect(toList());
    }
}
