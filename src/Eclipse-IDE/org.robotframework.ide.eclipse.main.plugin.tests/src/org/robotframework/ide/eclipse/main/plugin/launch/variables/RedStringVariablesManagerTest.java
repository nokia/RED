/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.variables;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RedStringVariablesManagerTest {

    private static final IStringVariableManager VARIABLE_MANAGER = VariablesPlugin.getDefault()
            .getStringVariableManager();

    private static final IValueVariable[] CUSTOM_VARIABLES = new IValueVariable[] {
            VARIABLE_MANAGER.newValueVariable("a", "", false, "0"),
            VARIABLE_MANAGER.newValueVariable("b", "", false, "1"),
            VARIABLE_MANAGER.newValueVariable("c", "", false, "2") };

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
    public void overridableVariablesMappingConsistOfNotContributedNonReadOnlyOnes() {
        final IValueVariable var1 = variable("var1", "val1", true, true);
        final IValueVariable var2 = variable("var2", "val2", true, false);
        final IValueVariable var3 = variable("var3", "val3", false, true);
        final IValueVariable var4 = variable("var4", "val4", false, false);
        final IValueVariable var5 = variable("var5", "val5", false, false);
        final IValueVariable var6 = variable("var6", "val6", false, false);

        final IStringVariableManager varManager = mock(IStringVariableManager.class);
        when(varManager.getValueVariables()).thenReturn(new IValueVariable[] { var1, var2, var3, var4, var5, var6 });

        final RedStringVariablesManager manager = new RedStringVariablesManager(varManager, null);
        final Map<String, String> vars = manager.getOverridableVariables();
        assertThat(vars).isInstanceOf(LinkedHashMap.class).hasSize(3)
                .containsEntry("var4", "val4")
                .containsEntry("var5", "val5")
                .containsEntry("var6", "val6");
        assertThat(vars.keySet()).containsExactly("var4", "var5", "var6");
    }

    @Test
    public void substitutionTest() throws JsonProcessingException, CoreException {
        final Map<String, List<List<String>>> input = new LinkedHashMap<>();
        input.put("set 1", newArrayList());
        input.get("set 1").add(newArrayList("a", "3"));
        input.get("set 1").add(newArrayList("b", "4"));
        input.get("set 1").add(newArrayList("c", "5"));
        input.put("set 2", newArrayList());
        input.get("set 2").add(newArrayList("a", "6"));
        input.get("set 2").add(newArrayList("c", "8"));

        final IPreferenceStore store = RedPlugin.getDefault().getPreferenceStore();
        store.putValue(RedPreferences.STRING_VARIABLES_SETS, new ObjectMapper().writeValueAsString(input));
        store.putValue(RedPreferences.STRING_VARIABLES_ACTIVE_SET, "set 1");
        
        final RedStringVariablesManager manager = new RedStringVariablesManager(VARIABLE_MANAGER,
                RedPlugin.getDefault().getPreferences());

        assertThat(manager.substitute("${a}${b}${c}")).isEqualTo("012");
        assertThat(manager.substituteUsingQuickValuesSet("${a}${b}${c}")).isEqualTo("345");
        store.putValue(RedPreferences.STRING_VARIABLES_ACTIVE_SET, "set 2");
        assertThat(manager.substituteUsingQuickValuesSet("${a}${b}${c}")).isEqualTo("618");
        store.putValue(RedPreferences.STRING_VARIABLES_ACTIVE_SET, "");
        assertThat(manager.substituteUsingQuickValuesSet("${a}${b}${c}")).isEqualTo("012");
    }

    private static IValueVariable variable(final String name, final String value, final boolean readOnly,
            final boolean contributed) {
        final IValueVariable var = mock(IValueVariable.class);
        when(var.getName()).thenReturn(name);
        when(var.getValue()).thenReturn(value);
        when(var.isReadOnly()).thenReturn(readOnly);
        when(var.isContributed()).thenReturn(contributed);
        return var;
    }
}
