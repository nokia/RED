/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model;

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
        assertThat(createCallsFromCaseForTest()).hasSize(5);
    }

    @Test
    public void testEmptyLinesCreationFromKeywordForTest() {
        assertThat(createCallsFromKeywordForTest()).hasSize(5);
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

    @Test
    public void emptyLineShouldNotBeCommented_whenNotCommented() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("")
                .appendLine("  Log  arg")
                .build();
        final List<RobotKeywordCall> calls = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren();
        assertThat(calls).hasSize(2);
        assertThat(calls.get(0).getName()).isEqualTo("");
        assertThat(calls.get(0).getComment()).isEmpty();
        assertThat(calls.get(0).shouldAddCommentMark()).isFalse();
    }

    private static void assertArguments(final List<RobotEmptyLine> list) {
        assertThat(list).allSatisfy(el -> assertThat(el.getArguments()).isEmpty());
    }

    private static void assertName(final List<RobotEmptyLine> list) {
        for (int i = 0; i < list.size(); i++) {
            if (i != 2) {
                assertThat(list.get(i).getName()).isEmpty();
            } else {
                assertThat(list.get(i).getName()).isEqualTo("  ");
            }
        }
    }

    private static void assertLabel(final List<RobotEmptyLine> list) {
        for (int i = 0; i < list.size(); i++) {
            if (i != 2) {
                assertThat(list.get(i).getLabel()).isEmpty();
            } else {
                assertThat(list.get(i).getLabel()).isEqualTo("  ");
            }
        }
    }

    private static void assertComment(final List<RobotEmptyLine> list) {
        assertThat(list).allSatisfy(el -> assertThat(el.getCommentTokens()).isEmpty());
    }

    @Test
    public void insertCell_shouldDoNothing() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("")
                .appendLine("  Log  arg")
                .build();
        final RobotKeywordCall emptyBefore = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);
        final List<RobotToken> emptyTokenBefore = emptyBefore.getLinkedElement().getElementTokens();

        emptyBefore.insertCellAt(0, "");

        final RobotKeywordCall emptyAfter = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);
        final List<RobotToken> emptyTokenAfter = emptyAfter.getLinkedElement().getElementTokens();

        assertThat(emptyAfter).isEqualTo(emptyBefore);
        assertThat(emptyTokenAfter).hasSameSizeAs(emptyTokenBefore);
        assertThat(emptyTokenAfter).hasSize(1);
        assertThat(emptyTokenAfter.get(0).getText().trim()).isEmpty();
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