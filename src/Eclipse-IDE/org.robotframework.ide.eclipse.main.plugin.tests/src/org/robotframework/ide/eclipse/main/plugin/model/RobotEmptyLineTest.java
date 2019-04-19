/*
* Copyright 2017 Nokia Solutions and Networks
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

import org.junit.Test;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;

public class RobotEmptyLineTest {

    @Test
    public void testEmptyLinesCreationFromCaseForTest() {
        assertThat(createCallsFromCaseForTest()).hasSize(6);
    }

    @Test
    public void testEmptyLinesCreationFromKeywordForTest() {
        assertThat(createCallsFromKeywordForTest()).hasSize(6);
    }

    @Test
    public void testEmptyLinesCreationFromSettingsForTest() {
        assertThat(createCallsFromSettingsForTest()).hasSize(0);
    }

    @Test
    public void testEmptyLinesCreationFromVariablesForTest() {
        assertThat(createCallsFromVariablesForTest()).hasSize(0);
    }

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
    public void testCommentsGettingForCaseCalls() {
        assertComment(createCallsFromCaseForTest());
    }

    @Test
    public void testCommentsGettingForKeywordCalls() {
        assertComment(createCallsFromKeywordForTest());
    }

    private static void assertArguments(final List<RobotEmptyLine> list) {
        assertThat(list).allMatch(el -> el.getArguments().isEmpty());
    }

    private static void assertName(final List<RobotEmptyLine> list) {
        assertThat(list).allMatch(line -> line.getName().isEmpty());
    }

    private static void assertLabel(final List<RobotEmptyLine> list) {
        assertThat(list).allMatch(line -> line.getLabel().isEmpty());
    }

    private static void assertComment(final List<RobotEmptyLine> list) {
        final List<String> comments = list.stream()
                .map(RobotEmptyLine::getCommentTokens)
                .map(List::stream)
                .map(s -> s.map(RobotToken::getText).collect(joining("")))
                .collect(toList());
        assertThat(comments).containsExactly("", "# whole line commented", "", "", "", "");
    }

    @Test
    public void copyBySerializationTest() {
        for (final RobotEmptyLine call : createCallsFromCaseForTest()) {
            assertThat(call).has(RobotKeywordCallConditions.properlySetParent()).has(filePositions());

            final RobotEmptyLine callCopy = ModelElementsSerDe.copy(call);

            assertThat(callCopy).isNotSameAs(call).has(nullParent()).has(noFilePositions());
            assertThat(callCopy.getName()).isEqualTo(call.getName());
            assertThat(callCopy.getArguments()).isEqualTo(call.getArguments());
            assertThat(callCopy.getComment()).isEqualTo(call.getComment());
        }
    }

    private static List<RobotEmptyLine> createCallsFromCaseForTest() {
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

    private static List<RobotEmptyLine> createCallsFromKeywordForTest() {
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

    private static List<RobotEmptyLine> createCallsFromSettingsForTest() {
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

    private static List<RobotEmptyLine> createCallsFromVariablesForTest() {
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

    private static List<RobotEmptyLine> getEmptyLinesFromSection(final RobotSuiteFileSection section) {
        return section.getChildren()
                .get(0)
                .getChildren()
                .stream()
                .filter(RobotEmptyLine.class::isInstance)
                .map(RobotEmptyLine.class::cast)
                .collect(Collectors.toList());
    }
}