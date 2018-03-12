/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;

import com.google.common.base.Joiner;

public class RobotSettingTest {

    @Test
    public void testProperObjectIsCreated() {
        for (final RobotKeywordCall setting : createSettingsForTest(new ArrayList<String>(), new ArrayList<String>())) {
            assertThat(setting).isExactlyInstanceOf(RobotSetting.class);
        }
        for (final RobotKeywordCall setting : createSettingsForTest(newArrayList("a"), new ArrayList<String>())) {
            assertThat(setting).isExactlyInstanceOf(RobotSetting.class);
        }
        for (final RobotKeywordCall setting : createSettingsForTest(newArrayList("a"), newArrayList("# c"))) {
            assertThat(setting).isExactlyInstanceOf(RobotSetting.class);
        }
        for (final RobotKeywordCall setting : createSettingsForTest(newArrayList("a", "b"), newArrayList("# c"))) {
            assertThat(setting).isExactlyInstanceOf(RobotSetting.class);
        }
        for (final RobotKeywordCall setting : createSettingsForTest(newArrayList("a", "b"), newArrayList("# c", "d"))) {
            assertThat(setting).isExactlyInstanceOf(RobotSetting.class);
        }
    }

    @Test
    public void testNameGetting() {
        assertNames(createSettingsForTest(new ArrayList<String>(), new ArrayList<String>()));
        assertNames(createSettingsForTest(newArrayList("a"), new ArrayList<String>()));
        assertNames(createSettingsForTest(newArrayList("a"), newArrayList("# c")));
        assertNames(createSettingsForTest(newArrayList("a", "b"), newArrayList("# c")));
        assertNames(createSettingsForTest(newArrayList("a", "b"), newArrayList("# c", "d")));
    }

    @Test
    public void testArgumentsGetting() {
        for (final RobotKeywordCall setting : createSettingsForTest(new ArrayList<String>(), new ArrayList<String>())) {
            assertThat(setting.getArguments()).isEmpty();
        }
        for (final RobotKeywordCall setting : createSettingsForTest(newArrayList("a"), new ArrayList<String>())) {
            assertThat(setting.getArguments()).containsExactly("a");
        }
        for (final RobotKeywordCall setting : createSettingsForTest(newArrayList("a"), newArrayList("# c"))) {
            assertThat(setting.getArguments()).containsExactly("a");
        }
        for (final RobotKeywordCall setting : createSettingsForTest(newArrayList("a", "b"), newArrayList("# c"))) {
            assertThat(setting.getArguments()).containsExactly("a", "b");
        }
        for (final RobotKeywordCall setting : createSettingsForTest(newArrayList("a", "b"), newArrayList("# c", "d"))) {
            assertThat(setting.getArguments()).containsExactly("a", "b");
        }
        for (final RobotKeywordCall setting : createSettingsForTest(newArrayList("a", "b", "c", "d", "e"),
                newArrayList("# c"))) {
            assertThat(setting.getArguments()).containsExactly("a", "b", "c", "d", "e");
        }
    }

    private static void assertNames(final List<RobotKeywordCall> settings) {
        assertThat(settings.get(0).getName()).isEqualTo("Metadata");
        assertThat(settings.get(1).getName()).isEqualTo("Library");
        assertThat(settings.get(2).getName()).isEqualTo("Variables");
        assertThat(settings.get(3).getName()).isEqualTo("Resource");
        assertThat(settings.get(4).getName()).isEqualTo("Suite Setup");
        assertThat(settings.get(5).getName()).isEqualTo("Suite Teardown");
        assertThat(settings.get(6).getName()).isEqualTo("Test Setup");
        assertThat(settings.get(7).getName()).isEqualTo("Test Teardown");
        assertThat(settings.get(8).getName()).isEqualTo("Test Template");
        assertThat(settings.get(9).getName()).isEqualTo("Test Timeout");
        assertThat(settings.get(10).getName()).isEqualTo("Force Tags");
        assertThat(settings.get(11).getName()).isEqualTo("Default Tags");
    }

    private static List<RobotKeywordCall> createSettingsForTest(final List<String> arguments,
            final List<String> comments) {

        final String argsToAdd = arguments.isEmpty() ? "" : "  " + Joiner.on("  ").join(arguments);
        final String commentsToAdd = comments.isEmpty() ? "" : "  " + Joiner.on("  ").join(comments);
        final String toAdd = argsToAdd + commentsToAdd;

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("*** Settings ***")
                .appendLine("Metadata" + toAdd)
                .appendLine("Library" + toAdd)
                .appendLine("Variables" + toAdd)
                .appendLine("Resource" + toAdd)
                .appendLine("Suite Setup" + toAdd)
                .appendLine("Suite Teardown" + toAdd)
                .appendLine("Test Setup" + toAdd)
                .appendLine("Test Teardown" + toAdd)
                .appendLine("Test Template" + toAdd)
                .appendLine("Test Timeout" + toAdd)
                .appendLine("Force Tags" + toAdd)
                .appendLine("Default Tags" + toAdd)
                .build();
        final RobotSettingsSection section = model.findSection(RobotSettingsSection.class).get();
        return section.getChildren();
    }
}
