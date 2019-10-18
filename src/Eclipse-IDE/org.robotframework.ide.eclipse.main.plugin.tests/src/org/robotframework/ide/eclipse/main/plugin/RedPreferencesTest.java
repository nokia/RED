/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.Condition;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.junit.Test;
import org.rf.ide.core.rflint.RfLintRuleConfiguration;
import org.rf.ide.core.rflint.RfLintViolationSeverity;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.FoldableElements;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.OverriddenVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.ElementOpenMode;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotTask.Priority;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RedTemplateContextType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;

public class RedPreferencesTest {

    @Test
    public void allPreferenceNamesStartWithRedPrefix() throws Exception {
        final RedPreferences preferences = new RedPreferences(mock(IPreferenceStore.class));
        final List<Field> preferenceNameFields = Arrays.stream(RedPreferences.class.getDeclaredFields())
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> Modifier.isFinal(f.getModifiers()))
                .filter(f -> f.getType().isAssignableFrom(String.class))
                .collect(Collectors.toList());

        for (final Field field : preferenceNameFields) {
            assertThat((String) field.get(preferences)).startsWith("red.");
        }
    }

    @Test
    public void templatesAreRetrievedFromTemplateStore() throws Exception {
        final RedPreferences preferences = new RedPreferences(mock(IPreferenceStore.class));
        final TemplateStore store = preferences.getTemplateStore();

        assertThat(store.getTemplates()).extracting(Template::getName)
                .containsOnly("New test", "New task", "New keyword", "Settings section", "For loop");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void contextTypesAreRetrievedFromTemplateContextTypeRegistry() throws Exception {
        final RedPreferences preferences = new RedPreferences(mock(IPreferenceStore.class));

        for (final String id : newArrayList(RedTemplateContextType.KEYWORD_CALL_CONTEXT_TYPE,
                RedTemplateContextType.NEW_SECTION_CONTEXT_TYPE, RedTemplateContextType.NEW_KEYWORD_CONTEXT_TYPE,
                RedTemplateContextType.NEW_TEST_CONTEXT_TYPE, RedTemplateContextType.NEW_TASK_CONTEXT_TYPE)) {
            assertThat(preferences.getTemplateContextTypeRegistry().getContextType(id))
                    .isExactlyInstanceOf(RedTemplateContextType.class);
        }
    }

    @Test
    public void verifyFoldableElements_allCombinations() {
        // just encode all combinations as 4 bit number
        for (int i = 0; i < 16; i++) {
            final IPreferenceStore store = mock(IPreferenceStore.class);
            when(store.getBoolean(RedPreferences.FOLDABLE_SECTIONS)).thenReturn((i & 8) != 0);
            when(store.getBoolean(RedPreferences.FOLDABLE_CASES)).thenReturn((i & 4) != 0);
            when(store.getBoolean(RedPreferences.FOLDABLE_KEYWORDS)).thenReturn((i & 2) != 0);
            when(store.getBoolean(RedPreferences.FOLDABLE_DOCUMENTATION)).thenReturn((i & 1) != 0);

            final RedPreferences preferences = new RedPreferences(store);

            final EnumSet<FoldableElements> elements = preferences.getFoldableElements();

            assertThat(elements).is(containingElementOnlyWhen(FoldableElements.SECTIONS, (i & 8) != 0));
            assertThat(elements).is(containingElementOnlyWhen(FoldableElements.CASES, (i & 4) != 0));
            assertThat(elements).is(containingElementOnlyWhen(FoldableElements.KEYWORDS, (i & 2) != 0));
            assertThat(elements).is(containingElementOnlyWhen(FoldableElements.DOCUMENTATION, (i & 1) != 0));
        }
    }

    @Test
    public void elementsOpenModeIsTakenFromStore_1() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.FILE_ELEMENTS_OPEN_MODE)).thenReturn(ElementOpenMode.OPEN_IN_SOURCE.name());

        final RedPreferences preferences = new RedPreferences(store);

        assertThat(preferences.getElementOpenMode()).isEqualTo(ElementOpenMode.OPEN_IN_SOURCE);
    }

    @Test
    public void elementsOpenModeIsTakenFromStore_2() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.FILE_ELEMENTS_OPEN_MODE)).thenReturn(ElementOpenMode.OPEN_IN_TABLES.name());

        final RedPreferences preferences = new RedPreferences(store);

        assertThat(preferences.getElementOpenMode()).isEqualTo(ElementOpenMode.OPEN_IN_TABLES);
    }

    @Test
    public void rfLintRulesFilesAreTakenFromStore_1() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.RFLINT_RULES_FILES)).thenReturn("");

        final RedPreferences preferences = new RedPreferences(store);
        assertThat(preferences.getRfLintRulesFiles()).isEmpty();
    }

    @Test
    public void rfLintRulesFilesAreTakenFromStore_2() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.RFLINT_RULES_FILES))
                .thenReturn("/path/to/firstfile.py;/path/to/sndfile.py");

        final RedPreferences preferences = new RedPreferences(store);
        assertThat(preferences.getRfLintRulesFiles()).containsExactly("/path/to/firstfile.py", "/path/to/sndfile.py");
    }

    @Test
    public void rfLintRulesConfigIsTakenFromStore_1() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.RFLINT_RULES_CONFIG_NAMES)).thenReturn("");
        when(store.getString(RedPreferences.RFLINT_RULES_CONFIG_SEVERITIES)).thenReturn("");
        when(store.getString(RedPreferences.RFLINT_RULES_CONFIG_ARGS)).thenReturn("");

        final RedPreferences preferences = new RedPreferences(store);
        assertThat(preferences.getRfLintRulesConfigs()).isEmpty();
    }

    @Test
    public void rfLintRulesConfigIsTakenFromStore_2() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.RFLINT_RULES_CONFIG_NAMES)).thenReturn("Rule1;Rule2");
        when(store.getString(RedPreferences.RFLINT_RULES_CONFIG_SEVERITIES)).thenReturn("IGNORE;ERROR");
        when(store.getString(RedPreferences.RFLINT_RULES_CONFIG_ARGS)).thenReturn("80;");

        final RedPreferences preferences = new RedPreferences(store);
        final Map<String, RfLintRuleConfiguration> rules = preferences.getRfLintRulesConfigs();
        assertThat(rules).hasSize(2);

        assertThat(rules.keySet()).containsOnly("Rule1", "Rule2");

        assertThat(rules.get("Rule1").getSeverity()).isEqualTo(RfLintViolationSeverity.IGNORE);
        assertThat(rules.get("Rule1").getArguments()).isEqualTo("80");

        assertThat(rules.get("Rule2").getSeverity()).isEqualTo(RfLintViolationSeverity.ERROR);
        assertThat(rules.get("Rule2").getArguments()).isEmpty();
    }

    @Test
    public void rfLintRulesConfigIsTakenFromStore_evenIfSomeOutdatedValuesExist() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.RFLINT_RULES_CONFIG_NAMES)).thenReturn("Rule1;Rule2;Rule3");
        when(store.getString(RedPreferences.RFLINT_RULES_CONFIG_SEVERITIES)).thenReturn("IGNORE;DEFAULT;OTHER");
        when(store.getString(RedPreferences.RFLINT_RULES_CONFIG_ARGS)).thenReturn("80;;100");

        final RedPreferences preferences = new RedPreferences(store);
        final Map<String, RfLintRuleConfiguration> rules = preferences.getRfLintRulesConfigs();
        assertThat(rules).hasSize(3);

        assertThat(rules.keySet()).containsOnly("Rule1", "Rule2", "Rule3");

        assertThat(rules.get("Rule1").getSeverity()).isEqualTo(RfLintViolationSeverity.IGNORE);
        assertThat(rules.get("Rule1").getArguments()).isEqualTo("80");

        assertThat(rules.get("Rule2").getSeverity()).isNull();
        assertThat(rules.get("Rule2").getArguments()).isEmpty();

        assertThat(rules.get("Rule3").getSeverity()).isNull();
        assertThat(rules.get("Rule3").getArguments()).isEqualTo("100");
    }

    @Test
    public void rfLintRulesAdditionalArgumentsAreTakenFromStore_1() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.RFLINT_ADDITIONAL_ARGUMENTS)).thenReturn("");

        final RedPreferences preferences = new RedPreferences(store);
        assertThat(preferences.getRfLintAdditionalArguments()).isEmpty();
    }

    @Test
    public void rfLintRulesAdditionalArgumentsAreTakenFromStore_2() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.RFLINT_ADDITIONAL_ARGUMENTS)).thenReturn("abc");

        final RedPreferences preferences = new RedPreferences(store);
        assertThat(preferences.getRfLintAdditionalArguments()).isEqualTo("abc");
    }

    @Test
    public void messageLogOutputLimitIsTakenFromStore_1() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getBoolean(RedPreferences.LIMIT_MSG_LOG_OUTPUT)).thenReturn(false);
        when(store.getInt(RedPreferences.LIMIT_MSG_LOG_LENGTH)).thenReturn(1000);

        final RedPreferences preferences = new RedPreferences(store);
        assertThat(preferences.getMessageLogViewLimit()).isEmpty();
    }

    @Test
    public void messageLogOutputLimitIsTakenFromStore_2() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getBoolean(RedPreferences.LIMIT_MSG_LOG_OUTPUT)).thenReturn(true);
        when(store.getInt(RedPreferences.LIMIT_MSG_LOG_LENGTH)).thenReturn(1000);

        final RedPreferences preferences = new RedPreferences(store);
        assertThat(preferences.getMessageLogViewLimit()).contains(1000);
    }

    @Test
    public void turnOffValidationIsTakenFromStore_1() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getBoolean(RedPreferences.TURN_OFF_VALIDATION)).thenReturn(true);

        final RedPreferences preferences = new RedPreferences(store);

        assertThat(preferences.isValidationTurnedOff()).isTrue();
    }

    @Test
    public void turnOffValidationIsTakenFromStore_2() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getBoolean(RedPreferences.TURN_OFF_VALIDATION)).thenReturn(false);

        final RedPreferences preferences = new RedPreferences(store);

        assertThat(preferences.isValidationTurnedOff()).isFalse();
    }

    @Test
    public void taskDetectionEnablementIsTakenFromStore_1() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getBoolean(RedPreferences.TASKS_DETECTION_ENABLED)).thenReturn(false);

        final RedPreferences preferences = new RedPreferences(store);

        assertThat(preferences.isTasksDetectionEnabled()).isFalse();
    }

    @Test
    public void taskDetectionEnablementIsTakenFromStore_2() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getBoolean(RedPreferences.TASKS_DETECTION_ENABLED)).thenReturn(true);

        final RedPreferences preferences = new RedPreferences(store);

        assertThat(preferences.isTasksDetectionEnabled()).isTrue();
    }

    @Test
    public void tasksPrioritiesAreTakenFromStore() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.TASKS_TAGS)).thenReturn("X;Y;Z");
        when(store.getString(RedPreferences.TASKS_PRIORITIES)).thenReturn("LOW;NORMAL;HIGH");

        final RedPreferences preferences = new RedPreferences(store);

        assertThat(preferences.getTaskTagsWithPriorities()).hasSize(3)
                .containsEntry("X", Priority.LOW)
                .containsEntry("Y", Priority.NORMAL)
                .containsEntry("Z", Priority.HIGH);
    }

    @Test
    public void activeVariablesSetIsTakenProperlyFromStore_1() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.STRING_VARIABLES_ACTIVE_SET)).thenReturn("");

        final RedPreferences preferences = new RedPreferences(store);
        assertThat(preferences.getActiveVariablesSet()).isEmpty();
    }

    @Test
    public void activeVariablesSetIsTakenProperlyFromStore_2() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.STRING_VARIABLES_ACTIVE_SET)).thenReturn("x");

        final RedPreferences preferences = new RedPreferences(store);
        assertThat(preferences.getActiveVariablesSet()).contains("x");
    }

    @Test
    public void overriddenVariablesSetIsTakenProperlyFromStore() throws JsonProcessingException {
        final Map<String, List<List<String>>> input = new LinkedHashMap<>();
        input.put("a", newArrayList());
        input.get("a").add(newArrayList("x", "1"));
        input.get("a").add(newArrayList("y", "2"));
        input.get("a").add(newArrayList("z", "3"));
        input.put("b", newArrayList());
        input.get("b").add(newArrayList("x;", "1"));
        input.get("b").add(newArrayList("y=", "2=2"));
        input.put("c;d", newArrayList());
        input.get("c;d").add(newArrayList("x", "\\"));

        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.STRING_VARIABLES_SETS))
                .thenReturn(new ObjectMapper().writeValueAsString(input));

        final RedPreferences preferences = new RedPreferences(store);

        assertThat(preferences.getOverriddenVariablesSets()).hasSize(3)
                .containsEntry("a",
                        newArrayList(
                                new OverriddenVariable("x", "1"),
                                new OverriddenVariable("y", "2"),
                                new OverriddenVariable("z", "3")))
                .containsEntry("b",
                        newArrayList(
                                new OverriddenVariable("x;", "1"),
                                new OverriddenVariable("y=", "2=2")))
                .containsEntry("c;d",
                        newArrayList(
                                new OverriddenVariable("x", "\\")));
    }

    @Test
    public void equalsColoringPreferences_areEquals() {
        final ColoringPreference pref1 = new ColoringPreference(new RGB(1, 2, 3), SWT.NONE);
        final ColoringPreference pref2 = new ColoringPreference(new RGB(1, 2, 3), SWT.NONE);

        assertThat(pref1).isEqualTo(pref2);
    }

    @Test
    public void launchEnvironmentVariablesAreTakenProperlyFromStore() throws JsonProcessingException {
        final Map<String, String> input = new LinkedHashMap<>();
        input.put("VAR_NAME", "some value");
        input.put("EMPTY", "");
        input.put("lower", "1234");
        input.put("CHARACTERS", "~!@#$%^&*()_+`-{}[];':\",./<>?");

        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.LAUNCH_ENVIRONMENT_VARIABLES))
                .thenReturn(new ObjectMapper().writeValueAsString(input));

        final RedPreferences preferences = new RedPreferences(store);

        assertThat(preferences.getLaunchEnvironmentVariables()).hasSize(4)
                .containsEntry("VAR_NAME", "some value")
                .containsEntry("EMPTY", "")
                .containsEntry("lower", "1234")
                .containsEntry("CHARACTERS", "~!@#$%^&*()_+`-{}[];':\",./<>?");
    }

    @Test
    public void formatterCellLengthLimitIsNegative_whenLimitingIsDisabled() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getBoolean(RedPreferences.FORMATTER_IGNORE_LONG_CELLS_ENABLED)).thenReturn(false);
        when(store.getInt(RedPreferences.FORMATTER_LONG_CELL_LENGTH_LIMIT)).thenReturn(42);

        final RedPreferences preferences = new RedPreferences(store);

        assertThat(preferences.getFormatterIgnoredCellLengthLimit()).isEqualTo(-1);
    }

    @Test
    public void formatterCellLengthLimitIsTakenFromStore_whenLimitingIsEnabled() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getBoolean(RedPreferences.FORMATTER_IGNORE_LONG_CELLS_ENABLED)).thenReturn(true);
        when(store.getInt(RedPreferences.FORMATTER_LONG_CELL_LENGTH_LIMIT)).thenReturn(42);

        final RedPreferences preferences = new RedPreferences(store);

        assertThat(preferences.getFormatterIgnoredCellLengthLimit()).isEqualTo(42);
    }

    private static <T extends Iterable<?>> Condition<T> containingElementOnlyWhen(final Object element,
            final boolean condition) {
        return new Condition<T>() {

            @Override
            public boolean matches(final T iterable) {
                if (condition) {
                    return Iterables.contains(iterable, element);
                } else {
                    return !Iterables.contains(iterable, element);
                }
            }
        };
    }
}
