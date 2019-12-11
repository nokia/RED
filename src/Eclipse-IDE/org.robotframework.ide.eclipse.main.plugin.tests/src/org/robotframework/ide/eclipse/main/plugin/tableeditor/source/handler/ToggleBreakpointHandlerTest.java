/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.assertj.core.api.Condition;
import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.ToggleBreakpointHandler.E4ToggleBreakpointHandler;

import com.google.common.collect.Range;

public class ToggleBreakpointHandlerTest {

    @Test
    public void breakpointsAreNotPossibleInVariablesSection() {
        final RobotSuiteFile model = new RobotSuiteFileCreator(new RobotVersion(3, 2)).appendLine("*** Variables ***")
                .appendLine("${a}  1")
                .appendLine("@{l}  2  3  4")
                .appendLine("&{d}  e=5  f=6")
                .build();

        assertThat(breakpointLinesFor(model, Range.closed(-1, 5)))
                .satisfies(noBreakpointsPossibleAt(-1, 0, 1, 2, 3, 4, 5));
    }

    @Test
    public void breakpointsArePossibleInExecutableSettings() {
        final RobotSuiteFile model = new RobotSuiteFileCreator(new RobotVersion(3, 2)).appendLine("*** Settings ***")
                .appendLine("Documentation  doc")
                .appendLine("Metadata  a  b")
                .appendLine("Force Tags  tag")
                .appendLine("Default Tags  tag")
                .appendLine("Suite Setup  keyword")
                .appendLine("Suite Teardown  keyword")
                .appendLine("Test Setup  hello")
                .appendLine("Test Teardown  goodbye")
                .appendLine("Test Timeout  5")
                .appendLine("Test Template  kw")
                .appendLine("Library  lib")
                .appendLine("Resource  res.robot")
                .appendLine("Variables  vars.py")
                .build();

        assertThat(breakpointLinesFor(model, Range.closed(-1, 15)))
                .satisfies(noBreakpointsPossibleAt(-1, 0, 1, 2, 3, 4, 5, 10, 11, 12, 13, 14, 15))
                .contains(breakpointMapping(6), breakpointMapping(7), breakpointMapping(8), breakpointMapping(9));
    }

    @Test
    public void breakpointsArePossibleInExecutableSettings_whenTheSettingsAreMultiline() {
        final RobotSuiteFile model = new RobotSuiteFileCreator(new RobotVersion(3, 2)).appendLine("*** Settings ***")
                .appendLine("Suite Setup")
                .appendLine("...  keyword")
                .appendLine("...  1  2")
                .appendLine("Suite Teardown  keyword")
                .appendLine("...  1  2")
                .appendLine("Test Setup  hello")
                .appendLine("...  1  2")
                .appendLine("Test Teardown")
                .appendLine("...  goodbye  1")
                .appendLine("...  2")
                .build();

        assertThat(breakpointLinesFor(model, Range.closed(-1, 12)))
                .satisfies(noBreakpointsPossibleAt(-1, 0, 1, 12))
                .contains(breakpointMapping(2), breakpointMapping(3, 2), breakpointMapping(4, 2), breakpointMapping(5),
                        breakpointMapping(6, 5), breakpointMapping(7), breakpointMapping(8, 7), breakpointMapping(9),
                        breakpointMapping(10, 9), breakpointMapping(11, 9));
    }

    @Test
    public void breakpointsArePossibleInExecutableRowsOfTestCase() {
        final RobotSuiteFile model = new RobotSuiteFileCreator(new RobotVersion(3, 2)).appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("# new style loop")
                .appendLine("  FOR  ${x}  IN RANGE  10")
                .appendLine("    Log  ${x}")
                .appendLine("  END")
                .appendLine("")
                .appendLine("  # old style loop")
                .appendLine("  :FOR  ${y}  IN RANGE  10")
                .appendLine("  \\  Log  ${y}")
                .appendLine("  ")
                .appendLine("  call  1  2  3")
                .appendLine("  Run Keyword  Log  1")
                .appendLine("  ${x}  ${y}=  Set Variable  1  2")
                .appendLine("")
                .build();

        assertThat(breakpointLinesFor(model, Range.closed(-1, 16)))
                .satisfies(noBreakpointsPossibleAt(-1, 0, 1, 2, 3, 7, 8, 11, 15))
                .contains(breakpointMapping(4), breakpointMapping(5), breakpointMapping(6), breakpointMapping(9),
                        breakpointMapping(10), breakpointMapping(12), breakpointMapping(13), breakpointMapping(14));
    }

    @Test
    public void breakpointsArePossibleInLocalExecutableSettingsOfTestCase() {
        final RobotSuiteFile model = new RobotSuiteFileCreator(new RobotVersion(3, 2)).appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Tags]  tag")
                .appendLine("  [Timeout]  5")
                .appendLine("  [Documentation]  doc")
                .appendLine("  [Template]  kw")
                .appendLine("  [Setup]  call  arg")
                .appendLine("  [Teardown]  call  tag")
                .appendLine("  [unknown]  something")
                .build();

        assertThat(breakpointLinesFor(model, Range.closed(-1, 10)))
                .satisfies(noBreakpointsPossibleAt(-1, 0, 1, 2, 3, 4, 5, 6, 9, 10))
                .contains(breakpointMapping(7), breakpointMapping(8));
    }

    @Test
    public void breakpointsArePossibleInTestCases_whenExecutablesAreInMultipleLines() {
        final RobotSuiteFile model = new RobotSuiteFileCreator(new RobotVersion(3, 2)).appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Setup]")
                .appendLine("  ...  keyword")
                .appendLine("  ...  1  2")
                .appendLine("  Keyword  arg1")
                .appendLine("  ...  arg2")
                .appendLine("  ${x}  ${y}=")
                .appendLine("  ...  Set Variable  1  2")
                .build();

        assertThat(breakpointLinesFor(model, Range.closed(-1, 10))).satisfies(noBreakpointsPossibleAt(-1, 0, 1, 2, 10))
                .contains(breakpointMapping(3), breakpointMapping(4, 3), breakpointMapping(5, 3),
                        breakpointMapping(6), breakpointMapping(7, 6), breakpointMapping(8), breakpointMapping(9, 8));
    }

    @Test
    public void breakpointsArePossibleInExecutableRowsOfTask() {
        final RobotSuiteFile model = new RobotSuiteFileCreator(new RobotVersion(3, 2)).appendLine("*** Tasks ***")
                .appendLine("task")
                .appendLine("# new style loop")
                .appendLine("  FOR  ${x}  IN RANGE  10")
                .appendLine("    Log  ${x}")
                .appendLine("  END")
                .appendLine("")
                .appendLine("  # old style loop")
                .appendLine("  :FOR  ${y}  IN RANGE  10")
                .appendLine("  \\  Log  ${y}")
                .appendLine("  ")
                .appendLine("  call  1  2  3")
                .appendLine("  Run Keyword  Log  1")
                .appendLine("  ${x}  ${y}=  Set Variable  1  2")
                .appendLine("")
                .build();

        assertThat(breakpointLinesFor(model, Range.closed(-1, 16)))
                .satisfies(noBreakpointsPossibleAt(-1, 0, 1, 2, 3, 7, 8, 11, 15))
                .contains(breakpointMapping(4), breakpointMapping(5), breakpointMapping(6), breakpointMapping(9),
                        breakpointMapping(10), breakpointMapping(12), breakpointMapping(13), breakpointMapping(14));
    }

    @Test
    public void breakpointsArePossibleInLocalExecutableSettingsOfTask() {
        final RobotSuiteFile model = new RobotSuiteFileCreator(new RobotVersion(3, 2)).appendLine("*** Tasks ***")
                .appendLine("task")
                .appendLine("  [Tags]  tag")
                .appendLine("  [Timeout]  5")
                .appendLine("  [Documentation]  doc")
                .appendLine("  [Template]  kw")
                .appendLine("  [Setup]  call  arg")
                .appendLine("  [Teardown]  call  tag")
                .appendLine("  [unknown]  something")
                .build();

        assertThat(breakpointLinesFor(model, Range.closed(-1, 10)))
                .satisfies(noBreakpointsPossibleAt(-1, 0, 1, 2, 3, 4, 5, 6, 9, 10))
                .contains(breakpointMapping(7), breakpointMapping(8));
    }

    @Test
    public void breakpointsArePossibleInTasks_whenExecutablesAreInMultipleLines() {
        final RobotSuiteFile model = new RobotSuiteFileCreator(new RobotVersion(3, 2)).appendLine("*** Tasks ***")
                .appendLine("task")
                .appendLine("  [Setup]")
                .appendLine("  ...  keyword")
                .appendLine("  ...  1  2")
                .appendLine("  Keyword  arg1")
                .appendLine("  ...  arg2")
                .appendLine("  ${x}  ${y}=")
                .appendLine("  ...  Set Variable  1  2")
                .build();

        assertThat(breakpointLinesFor(model, Range.closed(-1, 10))).satisfies(noBreakpointsPossibleAt(-1, 0, 1, 2, 10))
                .contains(breakpointMapping(3), breakpointMapping(4, 3), breakpointMapping(5, 3), breakpointMapping(6),
                        breakpointMapping(7, 6), breakpointMapping(8), breakpointMapping(9, 8));
    }

    @Test
    public void breakpointsArePossibleInExecutableRowsOfKeyword() {
        final RobotSuiteFile model = new RobotSuiteFileCreator(new RobotVersion(3, 2)).appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("# new style loop")
                .appendLine("  FOR  ${x}  IN RANGE  10")
                .appendLine("    Log  ${x}")
                .appendLine("  END")
                .appendLine("")
                .appendLine("  # old style loop")
                .appendLine("  :FOR  ${y}  IN RANGE  10")
                .appendLine("  \\  Log  ${y}")
                .appendLine("  ")
                .appendLine("  call  1  2  3")
                .appendLine("  Run Keyword  Log  1")
                .appendLine("  ${x}  ${y}=  Set Variable  1  2")
                .appendLine("")
                .build();

        assertThat(breakpointLinesFor(model, Range.closed(-1, 16)))
                .satisfies(noBreakpointsPossibleAt(-1, 0, 1, 2, 3, 7, 8, 11, 15))
                .contains(breakpointMapping(4), breakpointMapping(5), breakpointMapping(6), breakpointMapping(9),
                        breakpointMapping(10), breakpointMapping(12), breakpointMapping(13), breakpointMapping(14));
    }

    @Test
    public void breakpointsArePossibleInLocalTeardownSettingOfKeyword() {
        final RobotSuiteFile model = new RobotSuiteFileCreator(new RobotVersion(3, 2)).appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("  [Arguments]  ${a}")
                .appendLine("  [Timeout]  5")
                .appendLine("  [Documentation]  doc")
                .appendLine("  [Tags]  kw")
                .appendLine("  [Teardown]  call  tag")
                .appendLine("  [unknown]  something")
                .appendLine("  [Return]  1")
                .build();

        assertThat(breakpointLinesFor(model, Range.closed(-1, 10)))
                .satisfies(noBreakpointsPossibleAt(-1, 0, 1, 2, 3, 4, 5, 6, 8, 9, 10))
                .contains(breakpointMapping(7));
    }

    @Test
    public void breakpointsArePossibleInKeywords_whenExecutablesAreInMultipleLines() {
        final RobotSuiteFile model = new RobotSuiteFileCreator(new RobotVersion(3, 2)).appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Teardown]")
                .appendLine("  ...  keyword")
                .appendLine("  ...  1  2")
                .appendLine("  Keyword  arg1")
                .appendLine("  ...  arg2")
                .appendLine("  ${x}  ${y}=")
                .appendLine("  ...  Set Variable  1  2")
                .build();

        assertThat(breakpointLinesFor(model, Range.closed(-1, 10))).satisfies(noBreakpointsPossibleAt(-1, 0, 1, 2, 10))
                .contains(breakpointMapping(3), breakpointMapping(4, 3), breakpointMapping(5, 3), breakpointMapping(6),
                        breakpointMapping(7, 6), breakpointMapping(8), breakpointMapping(9, 8));
    }

    private static Map<Integer, Optional<Integer>> breakpointLinesFor(final RobotSuiteFile model,
            final Range<Integer> range) {
        final Map<Integer, Optional<Integer>> mappings = new HashMap<>();
        for (int i = range.lowerEndpoint(); i <= range.upperEndpoint(); i++) {
            mappings.put(i, E4ToggleBreakpointHandler.getPossibleBreakpointLine(model, i));
        }
        return mappings;
    }

    private static Entry<? extends Integer, ? extends Optional<Integer>> breakpointMapping(final int key) {
        // when breakpoint is placed in the same line where toggle was done
        return breakpointMapping(key, key);
    }

    private static Entry<? extends Integer, ? extends Optional<Integer>> breakpointMapping(final int key,
            final Integer value) {
        return new SimpleEntry<>(key, Optional.ofNullable(value));
    }

    private static Condition<? super Map<Integer, Optional<Integer>>> noBreakpointsPossibleAt(
            final int... linesWithoutBreakpoints) {
        return new Condition<>(m -> {
            for (final int line : linesWithoutBreakpoints) {
                if (m.get(line).isPresent()) {
                    return false;
                }
            }
            return true;
        }, "All values for given lines should be empty");
    }
}
