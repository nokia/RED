/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.filePositions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.noFilePositions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.nullParent;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;

public class RobotKeywordCallTest {

    @Test
    public void testNameGettingForCaseCalls() {
        assertName(createCallsFromCaseForTest());
    }

    @Test
    public void testNameGettingForKeywordCalls() {
        assertName(createCallsFromKeywordForTest());
    }

    @Test
    public void testLabelGettingForCaseCalls() {
        assertLabel(createCallsFromCaseForTest());
    }

    @Test
    public void testLabelGettingForKeywordCalls() {
        assertLabel(createCallsFromKeywordForTest());
    }

    @Test
    public void testArgumentsGettingForCaseCalls() {
        assertArguments(createCallsFromCaseForTest());
    }

    @Test
    public void testArgumentsGettingForKeywordCalls() {
        assertArguments(createCallsFromKeywordForTest());
    }

    @Test
    public void testArgumentsForKeywordCallFollowedByCommentedSection() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  Log  t")
                .appendLine("  ")
                .appendLine("  ")
                .appendLine("# *** Settings ***")
                .appendLine("# Documentation    set3  set4")
                .build();
        final List<RobotKeywordCall> calls = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren();
        assertThat(calls).hasSize(4);
        assertThat(calls.get(0).getName()).isEqualTo("Log");
        assertThat(calls.get(0).getArguments()).containsExactly("t");
        assertThat(calls.get(0).getComment()).isEmpty();
        assertThat(calls.get(1).getName()).isEmpty();
        assertThat(calls.get(1).getArguments()).isEmpty();
        assertThat(calls.get(1).getComment()).isEmpty();
        assertThat(calls.get(2).getName()).isEmpty();
        assertThat(calls.get(2).getArguments()).isEmpty();
        assertThat(calls.get(2).getComment()).isEmpty();
        assertThat(calls.get(3).getName()).isEmpty();
        assertThat(calls.get(3).getArguments()).isEmpty();
        assertThat(calls.get(3).getComment()).isEqualTo("# *** Settings ***");
    }

    @Test
    public void testEmptyLinesCreationFromCaseForTest() {
        assertThat(createEmptyLinesFromCaseForTest()).hasSize(6);
    }

    @Test
    public void testEmptyLinesCreationFromKeywordForTest() {
        assertThat(createEmptyLinesFromKeywordForTest()).hasSize(6);
    }

    @Test
    public void testEmptyLinesCreationFromSettingsForTest() {
        assertThat(createEmptyLinesFromSettingsForTest()).hasSize(0);
    }

    @Test
    public void testEmptyLinesCreationFromVariablesForTest() {
        assertThat(createEmptyLinesFromVariablesForTest()).hasSize(0);
    }

    @Test
    public void testEmptyLineNameGettingForCaseCalls() {
        assertNamesAreEmpty(createEmptyLinesFromCaseForTest());
    }

    @Test
    public void testEmptyLineNameGettingForKeywordCalls() {
        assertNamesAreEmpty(createEmptyLinesFromKeywordForTest());
    }

    @Test
    public void testEmptyLineLabelGettingForCaseCalls() {
        assertLabelsAreEmpty(createEmptyLinesFromCaseForTest());
    }

    @Test
    public void testEmptyLineLabelGettingForKeywordCalls() {
        assertLabelsAreEmpty(createEmptyLinesFromKeywordForTest());
    }

    @Test
    public void testEmptyLineArgumentsGettingForCaseCalls() {
        assertArgumentsAreEmpty(createEmptyLinesFromCaseForTest());
    }

    @Test
    public void testEmptyLineArgumentsGettingForKeywordCalls() {
        assertArgumentsAreEmpty(createEmptyLinesFromKeywordForTest());
    }

    @Test
    public void testEmptyLineCommentsGettingForCaseCalls() {
        assertComment(createEmptyLinesFromCaseForTest());
    }

    @Test
    public void testEmptyLineCommentsGettingForKeywordCalls() {
        assertComment(createEmptyLinesFromKeywordForTest());
    }

    private static void assertArguments(final List<RobotKeywordCall> calls) {
        assertThat(calls.get(0).getArguments()).isEmpty();
        assertThat(calls.get(1).getArguments()).isEmpty();
        assertThat(calls.get(2).getArguments()).containsExactly("1");
        assertThat(calls.get(3).getArguments()).containsExactly("1");
        assertThat(calls.get(4).getArguments()).containsExactly("1", "2");
        assertThat(calls.get(5).getArguments()).containsExactly("1", "2");
        assertThat(calls.get(6).getArguments()).containsExactly("kw6", "1", "2");
        assertThat(calls.get(7).getArguments()).containsExactly("kw7", "1", "2");
        assertThat(calls.get(8).getArguments()).containsExactly("kw8", "1", "2");
        assertThat(calls.get(9).getArguments()).containsExactly("kw9", "1", "2");
        assertThat(calls.get(10).getArguments()).containsExactly("${y}", "kw10", "1", "2");
        assertThat(calls.get(11).getArguments()).containsExactly("${y}", "kw11", "1", "2");
        assertThat(calls.get(12).getArguments()).containsExactly("${y}=", "kw12", "1", "2");
        assertThat(calls.get(13).getArguments()).containsExactly("${y}=", "kw13", "1", "2");
    }

    private static void assertName(final List<RobotKeywordCall> calls) {
        assertThat(calls.get(0).getName()).isEqualTo("kw0");
        assertThat(calls.get(1).getName()).isEqualTo("kw1");
        assertThat(calls.get(2).getName()).isEqualTo("kw2");
        assertThat(calls.get(3).getName()).isEqualTo("kw3");
        assertThat(calls.get(4).getName()).isEqualTo("kw4");
        assertThat(calls.get(5).getName()).isEqualTo("kw5");
        assertThat(calls.get(6).getName()).isEqualTo("${x}");
        assertThat(calls.get(7).getName()).isEqualTo("${x}");
        assertThat(calls.get(8).getName()).isEqualTo("${x}=");
        assertThat(calls.get(9).getName()).isEqualTo("${x}=");
        assertThat(calls.get(10).getName()).isEqualTo("${x}");
        assertThat(calls.get(11).getName()).isEqualTo("${x}");
        assertThat(calls.get(12).getName()).isEqualTo("${x}");
        assertThat(calls.get(13).getName()).isEqualTo("${x}");
    }

    private static void assertLabel(final List<RobotKeywordCall> calls) {
        int i = 0;
        for (final RobotKeywordCall call : calls) {
            if (i != 14) {
                assertThat(call.getLabel()).isEqualTo("kw" + i);
                i++;
            } else { // comment case
                assertThat(call.getLabel()).isEqualTo("");
            }
        }
    }

    private static void assertArgumentsAreEmpty(final List<RobotKeywordCall> list) {
        assertThat(list).allMatch(el -> el.getArguments().isEmpty());
    }

    private static void assertNamesAreEmpty(final List<RobotKeywordCall> list) {
        assertThat(list).allMatch(line -> line.getName().isEmpty());
    }

    private static void assertLabelsAreEmpty(final List<RobotKeywordCall> list) {
        assertThat(list).allMatch(line -> line.getLabel().isEmpty());
    }

    private static void assertComment(final List<RobotKeywordCall> list) {
        final List<String> comments = list.stream()
                .map(RobotKeywordCall::getCommentTokens)
                .map(List::stream)
                .map(s -> s.map(RobotToken::getText).collect(joining("")))
                .collect(toList());
        assertThat(comments).containsExactly("", "# whole line commented", "", "", "", "");
    }

    @Test
    public void copyBySerializationTest() {
        for (final RobotKeywordCall call : createCallsFromCaseForTest()) {
            assertThat(call).has(RobotKeywordCallConditions.properlySetParent()).has(filePositions());

            final RobotKeywordCall callCopy = ModelElementsSerDe.copy(call);

            assertThat(callCopy).isNotSameAs(call).has(nullParent()).has(noFilePositions());

            assertThat(callCopy.getName()).isEqualTo(call.getName());
            assertThat(callCopy.getArguments()).isEqualTo(call.getArguments());
            assertThat(callCopy.getComment()).isEqualTo(call.getComment());
        }
    }

    @Test
    public void copyEmptyLinesBySerializationTest() {
        for (final RobotKeywordCall call : createEmptyLinesFromCaseForTest()) {
            assertThat(call).has(RobotKeywordCallConditions.properlySetParent()).has(filePositions());

            final RobotKeywordCall callCopy = ModelElementsSerDe.copy(call);

            assertThat(callCopy).isNotSameAs(call).has(nullParent()).has(noFilePositions());
            assertThat(callCopy.getName()).isEqualTo(call.getName());
            assertThat(callCopy.getArguments()).isEqualTo(call.getArguments());
            assertThat(callCopy.getComment()).isEqualTo(call.getComment());
        }
    }

    private static List<RobotKeywordCall> createCallsFromCaseForTest() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case1")
                .appendLine("  kw0")
                .appendLine("  kw1  # comment")
                .appendLine("  kw2  1")
                .appendLine("  kw3  1  # comment    rest")
                .appendLine("  kw4  1  2")
                .appendLine("  kw5  1  2  # comment    rest")
                .appendLine("  ${x}  kw6  1  2")
                .appendLine("  ${x}  kw7  1  2  # comment    rest")
                .appendLine("  ${x}=  kw8  1  2")
                .appendLine("  ${x}=  kw9  1  2  # comment    rest")
                .appendLine("  ${x}  ${y}  kw10  1  2")
                .appendLine("  ${x}  ${y}  kw11  1  2  # comment    rest")
                .appendLine("  ${x}  ${y}=  kw12  1  2")
                .appendLine("  ${x}  ${y}=  kw13  1  2  # comment    rest")
                .appendLine("  # whole line commented")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section.getChildren().get(0).getChildren();
    }

    private static List<RobotKeywordCall> createCallsFromKeywordForTest() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw1")
                .appendLine("  kw0")
                .appendLine("  kw1  # comment")
                .appendLine("  kw2  1")
                .appendLine("  kw3  1  # comment  rest")
                .appendLine("  kw4  1  2")
                .appendLine("  kw5  1  2  # comment  rest")
                .appendLine("  ${x}  kw6  1  2")
                .appendLine("  ${x}  kw7  1  2  # comment  rest")
                .appendLine("  ${x}=  kw8  1  2")
                .appendLine("  ${x}=  kw9  1  2  # comment  rest")
                .appendLine("  ${x}  ${y}  kw10  1  2")
                .appendLine("  ${x}  ${y}  kw11  1  2  # comment  rest")
                .appendLine("  ${x}  ${y}=  kw12  1  2")
                .appendLine("  ${x}  ${y}=  kw13  1  2  # comment  rest")
                .appendLine("  # whole line commented")
                .build();
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        return section.getChildren().get(0).getChildren();
    }

    private static List<RobotKeywordCall> createEmptyLinesFromCaseForTest() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case1")
                .appendLine("")
                .appendLine("  # whole line commented")
                .appendLine("")
                .appendLine("  ")
                .appendLine("")
                .appendLine("")
                .appendLine("  Log  arg  #comment")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return getEmptyLinesFromSection(section);
    }

    private static List<RobotKeywordCall> createEmptyLinesFromKeywordForTest() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw1")
                .appendLine("")
                .appendLine("  # whole line commented")
                .appendLine("")
                .appendLine("  ")
                .appendLine("")
                .appendLine("")
                .appendLine("  Log  arg  #comment")
                .build();
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        return getEmptyLinesFromSection(section);
    }

    private static List<RobotKeywordCall> createEmptyLinesFromSettingsForTest() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Metadata  sth")
                .appendLine("")
                .appendLine("  # whole line commented")
                .appendLine("")
                .appendLine("  ")
                .appendLine("")
                .appendLine("")
                .appendLine("Default Tags  tag  #comment")
                .build();
        final RobotSettingsSection section = model.findSection(RobotSettingsSection.class).get();
        return getEmptyLinesFromSection(section);
    }

    private static List<RobotKeywordCall> createEmptyLinesFromVariablesForTest() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${var}  12")
                .appendLine("")
                .appendLine("  # whole line commented")
                .appendLine("")
                .appendLine("  ")
                .appendLine("")
                .appendLine("")
                .appendLine("@{list}  item1  item2  #comment")
                .appendLine("&{dict}  key=value  #comment")
                .build();
        final RobotVariablesSection section = model.findSection(RobotVariablesSection.class).get();
        return getEmptyLinesFromSection(section);
    }

    private static List<RobotKeywordCall> getEmptyLinesFromSection(final RobotSuiteFileSection section) {
        return section.getChildren()
                .get(0)
                .getChildren()
                .stream()
                .filter(RobotKeywordCall.class::isInstance)
                .map(RobotKeywordCall.class::cast)
                .filter(RobotKeywordCall::isEmptyLine)
                .collect(Collectors.toList());
    }
}
