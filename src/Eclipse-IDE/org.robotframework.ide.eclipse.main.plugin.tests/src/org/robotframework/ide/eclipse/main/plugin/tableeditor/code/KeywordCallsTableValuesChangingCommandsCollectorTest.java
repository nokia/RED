/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.collect.ImmutableMap;

public class KeywordCallsTableValuesChangingCommandsCollectorTest {

    private IEventBroker eventBroker;

    private final KeywordCallsTableValuesChangingCommandsCollector collector = new KeywordCallsTableValuesChangingCommandsCollector();

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void insertingCellDoesNothing_whenLineIsEmpty() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("")
                .appendLine("  Log  msg")
                .build();
        final RobotCase robotCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final RobotKeywordCall orignalCall = getChild(robotCase, 0);

        final List<EditorCommand> undoCmds = insertCell(orignalCall, 0);

        final RobotKeywordCall callAfterExecution = getChild(robotCase, 0);
        assertThat(getCells(callAfterExecution)).containsExactly("");

        for (final EditorCommand toUndo : undoCmds) {
            toUndo.execute();
        }

        final RobotKeywordCall callAfterUndo = getChild(robotCase, 0);
        assertThat(getCells(callAfterExecution)).containsExactly("");

        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED), eq(
                ImmutableMap.of(IEventBroker.DATA, robotCase, RobotModelEvents.ADDITIONAL_DATA, callAfterExecution)));
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED),
                eq(ImmutableMap.of(IEventBroker.DATA, robotCase, RobotModelEvents.ADDITIONAL_DATA, callAfterUndo)));
    }

    @Test
    public void cellInsertedIntoCall_atFirstPosition() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  Log  t")
                .appendLine("  Log  t  # comment after kw")
                .appendLine("  # line with comment only")
                .build();
        final RobotCase robotCase = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0);
        final List<RobotKeywordCall> callsBefore = robotCase.getChildren();

        for (final RobotKeywordCall call : callsBefore) {
            insertCell(call, 0);
        }

        final List<RobotKeywordCall> callsAfter = robotCase.getChildren();

        assertThat(callsBefore).hasSameSizeAs(callsBefore);
        assertThat(callsAfter).hasSize(3);
        assertThat(getCells(getChild(robotCase, 0))).containsExactly("", "Log", "t");
        assertThat(getCells(getChild(robotCase, 1))).containsExactly("", "Log", "t", "# comment after kw");
        assertThat(getCells(getChild(robotCase, 2))).containsExactly("", "# line with comment only");
    }

    @Test
    public void cellInsertedIntoCall_inTheMiddle() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  Log  t")
                .appendLine("  Log  t  # comment after kw")
                .appendLine("  # line  with  comment  only")
                .build();
        final RobotCase robotCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final List<RobotKeywordCall> callsBefore = robotCase.getChildren();

        for (final RobotKeywordCall call : callsBefore) {
            insertCell(call, 1);
        }

        final List<RobotKeywordCall> callsAfter = robotCase.getChildren();

        assertThat(callsBefore).hasSameSizeAs(callsBefore);
        assertThat(callsAfter).hasSize(3);
        assertThat(getCells(getChild(robotCase, 0))).containsExactly("Log", "", "t");
        assertThat(getCells(getChild(robotCase, 1))).containsExactly("Log", "", "t", "# comment after kw");
        assertThat(getCells(getChild(robotCase, 2))).containsExactly("# line", "", "with", "comment", "only");
    }

    @Test
    public void cellInsertedIntoCall_atTheFirstCellOfTheComment_atTheEndOfLine() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  Log  t  # comment after kw")
                .build();
        final RobotCase robotCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final RobotKeywordCall callBefore = robotCase.getChildren().get(0);

        insertCell(callBefore, 2);

        final RobotKeywordCall callAfter = robotCase.getChildren().get(0);

        assertThat(callBefore).isEqualTo(callAfter);
        assertThat(getCells(getChild(robotCase, 0))).containsExactly("Log", "t", "", "# comment after kw");
    }

    @Test
    public void cellInsertedIntoSetting_atFirstPosition() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Template]  arg1  arg2  arg3  #comment1  comment2")
                .build();
        final RobotCase robotCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final RobotDefinitionSetting settingBefore = (RobotDefinitionSetting) getChild(robotCase, 0);
        assertThat(getCells(settingBefore)).containsExactly("[Template]", "arg1", "arg2", "arg3", "#comment1",
                "comment2");

        insertCell(settingBefore, 0);

        assertThat(getCells(getChild(robotCase, 0))).containsExactly("", "[Template]", "arg1", "arg2", "arg3",
                "#comment1", "comment2");
    }

    @Test
    public void cellInsertedIntoSetting_atFirstArgumentPosition() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Template]  arg1  arg2  arg3  #comment1  comment2")
                .build();
        final RobotCase robotCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final RobotDefinitionSetting settingBefore = (RobotDefinitionSetting) getChild(robotCase, 0);
        assertThat(getCells(settingBefore)).containsExactly("[Template]", "arg1", "arg2", "arg3", "#comment1",
                "comment2");

        insertCell(settingBefore, 1);

        assertThat(getCells(getChild(robotCase, 0))).containsExactly("[Template]", "", "arg1", "arg2", "arg3",
                "#comment1", "comment2");
    }

    @Test
    public void cellInsertedIntoSetting_atMiddleArgumentPosition() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Template]  arg1  arg2  arg3  #comment1  comment2")
                .build();
        final RobotCase robotCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final RobotDefinitionSetting settingBefore = (RobotDefinitionSetting) getChild(robotCase, 0);
        assertThat(getCells(settingBefore)).containsExactly("[Template]", "arg1", "arg2", "arg3", "#comment1",
                "comment2");

        insertCell(settingBefore, 2);

        assertThat(getCells(getChild(robotCase, 0))).containsExactly("[Template]", "arg1", "", "arg2", "arg3",
                "#comment1", "comment2");
    }

    @Test
    public void cellInsertedIntoSetting_atLastArgumentPosition() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Template]  arg1  arg2  arg3  #comment1  comment2")
                .build();
        final RobotCase robotCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final RobotDefinitionSetting settingBefore = (RobotDefinitionSetting) getChild(robotCase, 0);
        assertThat(getCells(settingBefore)).containsExactly("[Template]", "arg1", "arg2", "arg3", "#comment1",
                "comment2");

        insertCell(settingBefore, 3);

        assertThat(getCells(getChild(robotCase, 0))).containsExactly("[Template]", "arg1", "arg2", "", "arg3",
                "#comment1", "comment2");
    }

    @Test
    public void cellInsertedIntoSetting_atFirstCommentPosition() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Template]  arg1  arg2  arg3  #comment1  comment2")
                .build();
        final RobotCase robotCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final RobotDefinitionSetting settingBefore = (RobotDefinitionSetting) getChild(robotCase, 0);
        assertThat(getCells(settingBefore)).containsExactly("[Template]", "arg1", "arg2", "arg3", "#comment1",
                "comment2");

        insertCell(settingBefore, 4);

        assertThat(getCells(getChild(robotCase, 0))).containsExactly("[Template]", "arg1", "arg2", "arg3", "",
                "#comment1", "comment2");
    }

    @Test
    public void cellInsertedIntoSetting_atLastCommentPosition() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Template]  arg1  arg2  arg3  #comment1  comment2")
                .build();
        final RobotCase robotCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final RobotDefinitionSetting settingBefore = (RobotDefinitionSetting) getChild(robotCase, 0);
        assertThat(getCells(settingBefore)).containsExactly("[Template]", "arg1", "arg2", "arg3", "#comment1",
                "comment2");

        insertCell(settingBefore, 5);

        assertThat(getCells(getChild(robotCase, 0))).containsExactly("[Template]", "arg1", "arg2", "arg3", "#comment1",
                "", "comment2");
    }

    private RobotKeywordCall getChild(final RobotCase robotCase, final int childIndex) {
        return robotCase.getChildren().get(childIndex);
    }

    private List<String> getCells(final RobotKeywordCall call) {
        return ExecutablesRowView.rowData(call);
    }

    private List<EditorCommand> insertCell(final RobotKeywordCall call, final int index) {
        final Optional<? extends EditorCommand> command = collector.collectForInsertion(call, index);
        assertThat(command).isPresent();

        final EditorCommand cmd = command.get();
        cmd.setEventBroker(eventBroker);
        cmd.execute();

        return cmd.getUndoCommands();
    }
}
