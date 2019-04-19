/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.collect.ImmutableMap;

public class InsertCellCommandTest {

    private IEventBroker eventBroker;

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void insertCell_atDifferentCallPositions_inImports() {
        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine("*** Settings ***")
                .appendLine("Library  lib  arg1  arg2  arg3")
                .appendLine("Resource  res.robot  arg1  arg2  arg3")
                .appendLine("Variables  var.py  arg1  arg2  arg3")
                .build();
        final RobotSettingsSection robotImports = model.findSection(RobotSettingsSection.class).get();
        for (final RobotKeywordCall call : robotImports.getChildren()) {
            final int index = call.getIndex();
            final List<RobotToken> callTokens = call.getLinkedElement().getElementTokens();
            final List<String> allLabels = callTokens.stream().map(RobotToken::getText).collect(Collectors.toList());
            final int tokensNumber = callTokens.size();
            for (int i = 2; i < tokensNumber; i++) {
                final InsertCellCommand command = new InsertCellCommand((RobotSetting) call, i);
                command.setEventBroker(eventBroker);
                command.execute();

                final RobotKeywordCall callAfter = robotImports.getChildren().get(index);
                assertThat_valueAtPosition_wasInserted(i, allLabels, callAfter, "");
                undo_andAssertThat_valueDisappeared_afterUndo(command, allLabels, callAfter);
            }
            verify(eventBroker, times(2 * (tokensNumber - 2))).send(eq(RobotModelEvents.ROBOT_SETTING_CHANGED),
                    eq(ImmutableMap.of(IEventBroker.DATA, robotImports, RobotModelEvents.ADDITIONAL_DATA, call)));
        }
    }

    @Test
    public void insertCell_atDifferentCallPositions_inGeneralSettings() {
        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine("*** Settings ***")
                .appendLine("Suite Setup  kw  arg1  arg2")
                .appendLine("Suite Teardown  kw  arg1  arg2")
                .appendLine("Test Setup  kw  arg1  arg2")
                .appendLine("Test Teardown  kw  arg1  arg2")
                .appendLine("Test Template  kw  arg1  arg2")
                .appendLine("Test Timeout  12")
                .appendLine("Force Tags  tag1  tag2  tag3")
                .appendLine("Default Tags  tag1  tag2  tag3")
                .build();
        final RobotSettingsSection robotImports = model.findSection(RobotSettingsSection.class).get();
        for (final RobotKeywordCall call : robotImports.getChildren()) {
            final int index = call.getIndex();
            final List<RobotToken> callTokens = call.getLinkedElement().getElementTokens();
            final List<String> allLabels = callTokens.stream().map(RobotToken::getText).collect(Collectors.toList());
            final int tokensNumber = callTokens.size();
            for (int i = 2; i < tokensNumber; i++) {
                final InsertCellCommand command = new InsertCellCommand((RobotSetting) call, i);
                command.setEventBroker(eventBroker);
                command.execute();

                final RobotKeywordCall callAfter = robotImports.getChildren().get(index);
                assertThat_valueAtPosition_wasInserted(i, allLabels, callAfter, "");
                undo_andAssertThat_valueDisappeared_afterUndo(command, allLabels, callAfter);
            }
            verify(eventBroker, times(2 * (tokensNumber - 2))).send(eq(RobotModelEvents.ROBOT_SETTING_CHANGED),
                    eq(ImmutableMap.of(IEventBroker.DATA, robotImports, RobotModelEvents.ADDITIONAL_DATA, call)));
        }
    }

    private void assertThat_valueAtPosition_wasInserted(final int insertedPosition, final List<String> allLabels,
            final RobotKeywordCall call, final String value) {
        final List<String> currentLabels = call.getLinkedElement().getElementTokens().stream().map(RobotToken::getText)
                .collect(Collectors.toList());
        final List<String> oneMoreLabels = new ArrayList<>(allLabels);
        oneMoreLabels.add(insertedPosition, value);

        assertThat(currentLabels).containsExactlyElementsOf(oneMoreLabels);
    }

    private void undo_andAssertThat_valueDisappeared_afterUndo(final InsertCellCommand executed,
            final List<String> allLabels, final RobotKeywordCall call) {
        final IRobotCodeHoldingElement parent = call.getParent();
        final int index = call.getIndex();
        for (final EditorCommand command : executed.getUndoCommands()) {
            command.execute();
        }
        final List<String> currentLabels = parent.getChildren().get(index).getLinkedElement().getElementTokens()
                .stream().map(RobotToken::getText).collect(Collectors.toList());

        assertThat(currentLabels).containsExactlyElementsOf(allLabels);
    }
}
