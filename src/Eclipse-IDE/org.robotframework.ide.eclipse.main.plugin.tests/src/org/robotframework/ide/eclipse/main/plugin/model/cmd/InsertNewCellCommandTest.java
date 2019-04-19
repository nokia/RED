/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class InsertNewCellCommandTest {

    private IEventBroker eventBroker;

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void insertCell_atDifferentCallPositions_inTestCases() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  call  single arg")
                .appendLine("  call  1  2  #comment  sth")
                .build();
        final RobotCase robotCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        for (final RobotKeywordCall call : robotCase.getChildren()) {
            final int index = call.getIndex();
            final List<RobotToken> callTokens = call.getLinkedElement().getElementTokens();
            final List<String> allLabels = callTokens.stream().map(RobotToken::getText).collect(Collectors.toList());
            final int tokensNumber = callTokens.size();
            for (int i = 0; i < tokensNumber; i++) {
                final InsertNewCellCommand command = new InsertNewCellCommand(call, i);
                command.setEventBroker(eventBroker);
                command.execute();

                final RobotKeywordCall callAfter = robotCase.getChildren().get(index);
                assertThatValueAtPositionWasInserted(allLabels, callAfter, i, "");
                undoAndAssertThatValueDisappearedAfterUndo(command, allLabels, callAfter);
            }
            verify(eventBroker, times(2 * tokensNumber)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_CELL_CHANGE, call);
        }
    }

    @Test
    public void insertNonFirstCell_inCommentLineOnly_inTestCases() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  #comment  line  only")
                .build();
        final RobotCase robotCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final RobotKeywordCall call = robotCase.getChildren().get(0);

        final List<RobotToken> callTokens = call.getLinkedElement().getElementTokens();
        final List<String> allLabels = callTokens.stream()
                .map(RobotToken::getText)
                .collect(Collectors.toList());
        for (int i = 1; i < 3; i++) {
            final InsertNewCellCommand command = new InsertNewCellCommand(call, i);
            command.setEventBroker(eventBroker);
            command.execute();

            final RobotKeywordCall callAfter = robotCase.getChildren().get(0);
            assertThatValueAtPositionWasInserted(allLabels, callAfter, i, "");

            final IRobotCodeHoldingElement parent = call.getParent();
            final int index = call.getIndex();
            for (final EditorCommand toUndo : command.getUndoCommands()) {
                toUndo.execute();
            }
            final List<String> currentLabels = parent.getChildren()
                    .get(index)
                    .getLinkedElement()
                    .getElementTokens()
                    .stream()
                    .map(RobotToken::getText)
                    .collect(Collectors.toList());

            assertThat(currentLabels).containsExactlyElementsOf(allLabels);
        }

        verify(eventBroker, times(4)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_CELL_CHANGE, call);
    }

    @Test
    public void insertFirstCell_inCommentLineOnly_inTestCases() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  #comment  line  only")
                .build();
        final RobotCase robotCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final RobotKeywordCall call = robotCase.getChildren().get(0);

        final List<RobotToken> callTokens = call.getLinkedElement().getElementTokens();
        final List<String> allLabels = callTokens.stream().map(RobotToken::getText).collect(Collectors.toList());
        final InsertNewCellCommand command = new InsertNewCellCommand(call, 0);
        command.setEventBroker(eventBroker);
        command.execute();

        final RobotKeywordCall callAfter = robotCase.getChildren().get(0);
        assertThatValueAtPositionWasInserted(allLabels, callAfter, 0, "");

        final IRobotCodeHoldingElement parent = call.getParent();
        final int index = call.getIndex();
        for (final EditorCommand toUndo : command.getUndoCommands()) {
            toUndo.execute();
        }
        final List<String> currentLabels = parent.getChildren()
                .get(index)
                .getLinkedElement()
                .getElementTokens()
                .stream()
                .map(RobotToken::getText)
                .collect(Collectors.toList());

        assertThat(currentLabels).containsExactlyElementsOf(allLabels);

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_CELL_CHANGE, call);
    }

    @Test
    public void insertCell_atDifferentCallPositions_inKeywords() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keyword ***")
                .appendLine("kw")
                .appendLine("  call  single arg")
                .appendLine("  call  1  2  #comment  sth")
                .build();
        final RobotKeywordDefinition robotKeyword = model.findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0);
        for (final RobotKeywordCall call : robotKeyword.getChildren()) {
            final int index = call.getIndex();
            final List<RobotToken> callTokens = call.getLinkedElement().getElementTokens();
            final List<String> allLabels = callTokens.stream().map(RobotToken::getText).collect(Collectors.toList());
            final int tokensNumber = callTokens.size();
            for (int i = 0; i < tokensNumber; i++) {
                final InsertNewCellCommand command = new InsertNewCellCommand(call, i);
                command.setEventBroker(eventBroker);
                command.execute();

                final RobotKeywordCall callAfter = robotKeyword.getChildren().get(index);
                assertThatValueAtPositionWasInserted(allLabels, callAfter, i, "");
                undoAndAssertThatValueDisappearedAfterUndo(command, allLabels, callAfter);
            }
            verify(eventBroker, times(2 * tokensNumber)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_CELL_CHANGE, call);
        }
    }

    @Test
    public void insertNonFirstCell_inCommentLineOnly_inKeywords() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("  #comment  line  only")
                .build();
        final RobotKeywordDefinition robotKeyword = model.findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0);
        final RobotKeywordCall call = robotKeyword.getChildren().get(0);

        final List<RobotToken> callTokens = call.getLinkedElement().getElementTokens();
        final List<String> allLabels = callTokens.stream()
                .map(RobotToken::getText)
                .collect(Collectors.toList());
        for (int i = 1; i < 3; i++) {
            final InsertNewCellCommand command = new InsertNewCellCommand(call, i);
            command.setEventBroker(eventBroker);
            command.execute();

            final RobotKeywordCall callAfter = robotKeyword.getChildren().get(0);
            assertThatValueAtPositionWasInserted(allLabels, callAfter, i, "");

            final IRobotCodeHoldingElement parent = call.getParent();
            final int index = call.getIndex();
            for (final EditorCommand toUndo : command.getUndoCommands()) {
                toUndo.execute();
            }
            final List<String> currentLabels = parent.getChildren()
                    .get(index)
                    .getLinkedElement()
                    .getElementTokens()
                    .stream()
                    .map(RobotToken::getText)
                    .collect(Collectors.toList());

            assertThat(currentLabels).containsExactlyElementsOf(allLabels);
        }

        verify(eventBroker, times(4)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_CELL_CHANGE, call);
    }

    @Test
    public void insertFirstCell_inCommentLineOnly_inKeywords() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("  #comment  line  only")
                .build();
        final RobotKeywordDefinition robotKeyword = model.findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0);
        final RobotKeywordCall call = robotKeyword.getChildren().get(0);

        final List<RobotToken> callTokens = call.getLinkedElement().getElementTokens();
        final List<String> allLabels = callTokens.stream().map(RobotToken::getText).collect(Collectors.toList());
        final InsertNewCellCommand command = new InsertNewCellCommand(call, 0);
        command.setEventBroker(eventBroker);
        command.execute();

        final RobotKeywordCall callAfter = robotKeyword.getChildren().get(0);
        assertThatValueAtPositionWasInserted(allLabels, callAfter, 0, "");

        final IRobotCodeHoldingElement parent = call.getParent();
        final int index = call.getIndex();
        for (final EditorCommand toUndo : command.getUndoCommands()) {
            toUndo.execute();
        }
        final List<String> currentLabels = parent.getChildren()
                .get(index)
                .getLinkedElement()
                .getElementTokens()
                .stream()
                .map(RobotToken::getText)
                .collect(Collectors.toList());

        assertThat(currentLabels).containsExactlyElementsOf(allLabels);

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_CELL_CHANGE, call);
    }

    private void assertThatValueAtPositionWasInserted(final List<String> allLabels, final RobotKeywordCall call,
            final int insertedPosition, final String value) {
        final List<String> currentLabels = call.getLinkedElement()
                .getElementTokens()
                .stream()
                .map(RobotToken::getText)
                .collect(Collectors.toList());
        final List<String> oneMoreLabels = new ArrayList<>(allLabels);
        oneMoreLabels.add(insertedPosition, value);

        assertThat(currentLabels).containsExactlyElementsOf(oneMoreLabels);
    }

    private void undoAndAssertThatValueDisappearedAfterUndo(final InsertNewCellCommand executed,
            final List<String> allLabels, final RobotKeywordCall call) {
        final IRobotCodeHoldingElement parent = call.getParent();
        final int index = call.getIndex();
        executed.getUndoCommands().forEach(EditorCommand::execute);

        final List<String> currentLabels = parent.getChildren()
                .get(index)
                .getLinkedElement()
                .getElementTokens()
                .stream()
                .map(RobotToken::getText)
                .collect(Collectors.toList());

        assertThat(currentLabels).containsExactlyElementsOf(allLabels);
    }
}
