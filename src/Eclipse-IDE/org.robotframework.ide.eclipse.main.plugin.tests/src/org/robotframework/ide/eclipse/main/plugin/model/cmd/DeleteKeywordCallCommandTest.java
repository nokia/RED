/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

@RunWith(Theories.class)
public class DeleteKeywordCallCommandTest {

    @DataPoints
    public static RobotSuiteFileSection[] elements() {
        final RobotSuiteFile model = createModel();
        final RobotSuiteFileSection[] elements = new RobotSuiteFileSection[2];
        elements[0] = model.findSection(RobotCasesSection.class).get();
        elements[1] = model.findSection(RobotKeywordsSection.class).get();
        return elements;
    }

    @Theory
    public void nothingHappens_whenThereAreNoCallsToRemove(final RobotSuiteFileSection section) {
        final List<RobotKeywordCall> callsToRemove = newArrayList();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteKeywordCallCommand(callsToRemove))
                .execute();

        assertThat(section.getChildren()).hasSize(2);

        verifyZeroInteractions(eventBroker);
    }

    @Theory
    public void settingsAreProperlyRemoved_whenRemovingRowsFromSingleCase(final RobotSuiteFileSection section) {
        final RobotCodeHoldingElement<?> codeHolder = (RobotCodeHoldingElement<?>) section.getChildren().get(1);

        final List<RobotKeywordCall> callsToRemove = newArrayList(codeHolder.getChildren().get(0),
                codeHolder.getChildren().get(1), codeHolder.getChildren().get(2));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final DeleteKeywordCallCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteKeywordCallCommand(callsToRemove));
        command.execute();

        assertThat(codeHolder.getChildren()).hasSize(1);
        assertThat(codeHolder.getChildren().get(0).getName()).isEqualTo("Log");
        // assert that there are no settings
        if (codeHolder instanceof RobotCase) {
            final TestCase testCase = ((RobotCase) codeHolder).getLinkedElement();
            assertThat(testCase.getAllElements()).hasSameSizeAs(testCase.getExecutionContext());
        } else {
            final UserKeyword userKeyword = ((RobotKeywordDefinition) codeHolder).getLinkedElement();
            assertThat(userKeyword.getAllElements()).hasSameSizeAs(userKeyword.getExecutionContext());
        }

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, codeHolder);
    }

    @Theory
    public void executableRowsAreProperlyRemoved_whenRemovingRowsFromSingleCase(final RobotSuiteFileSection section) {
        final RobotCodeHoldingElement<?> codeHolder = (RobotCodeHoldingElement<?>) section.getChildren().get(0);

        final List<RobotKeywordCall> callsToRemove = newArrayList(codeHolder.getChildren().get(3),
                codeHolder.getChildren().get(4));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final DeleteKeywordCallCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteKeywordCallCommand(callsToRemove));
        command.execute();

        assertThat(codeHolder.getChildren()).hasSize(3);
        assertThat(codeHolder.getChildren().get(0).getName()).isEqualTo("Documentation");
        assertThat(codeHolder.getChildren().get(1).getName()).isEqualTo("Tags");
        assertThat(codeHolder.getChildren().get(2).getName()).isEqualTo("Teardown");
        if (codeHolder instanceof RobotCase) {
            assertThat(((RobotCase) codeHolder).getLinkedElement().getExecutionContext()).isEmpty();
        } else {
            assertThat(((RobotKeywordDefinition) codeHolder).getLinkedElement().getExecutionContext()).isEmpty();
        }

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, codeHolder);
    }

    @Theory
    public void rowsAreProperlyRemoved_whenRemovingFromDifferentCases(final RobotSuiteFileSection section) {
        final RobotCodeHoldingElement<?> codeHolder1 = (RobotCodeHoldingElement<?>) section.getChildren().get(0);
        final RobotCodeHoldingElement<?> codeHolder2 = (RobotCodeHoldingElement<?>) section.getChildren().get(1);

        final List<RobotKeywordCall> callsToRemove = newArrayList(codeHolder1.getChildren().get(1),
                codeHolder1.getChildren().get(3), codeHolder2.getChildren().get(0), codeHolder2.getChildren().get(2));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final DeleteKeywordCallCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteKeywordCallCommand(callsToRemove));
        command.execute();

        assertThat(codeHolder1.getChildren()).hasSize(3);
        assertThat(codeHolder1.getChildren().get(0).getName()).isEqualTo("Documentation");
        assertThat(codeHolder1.getChildren().get(1).getName()).isEqualTo("Teardown");
        assertThat(codeHolder1.getChildren().get(2).getName()).isEqualTo("Log");
        if (codeHolder1 instanceof RobotCase) {
            assertThat(((RobotCase) codeHolder1).getLinkedElement().getExecutionContext()).hasSize(1);
            assertThat(((RobotCase) codeHolder1).getLinkedElement().getTags()).isEmpty();
        } else {
            assertThat(((RobotKeywordDefinition) codeHolder1).getLinkedElement().getExecutionContext()).hasSize(1);
            assertThat(((RobotKeywordDefinition) codeHolder1).getLinkedElement().getTags()).isEmpty();
        }

        assertThat(codeHolder2.getChildren()).hasSize(2);
        assertThat(codeHolder2.getChildren().get(0).getName()).isEqualTo("Timeout");
        assertThat(codeHolder2.getChildren().get(1).getName()).isEqualTo("Log");
        if (codeHolder2 instanceof RobotCase) {
            assertThat(((RobotCase) codeHolder2).getLinkedElement().getSetups()).isEmpty();
            assertThat(((RobotCase) codeHolder2).getLinkedElement().getUnknownSettings()).isEmpty();
        } else {
            assertThat(((RobotKeywordDefinition) codeHolder2).getLinkedElement().getArguments()).isEmpty();
            assertThat(((RobotKeywordDefinition) codeHolder2).getLinkedElement().getUnknownSettings()).isEmpty();
        }

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, codeHolder1);
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, codeHolder2);
    }
    
    @Theory
    public void rowsAreProperlyRemovedAndReturnedToPreviousState_whenRemovingFromDifferentCases(
            final RobotSuiteFileSection section) {
        final RobotCodeHoldingElement<?> codeHolder1 = (RobotCodeHoldingElement<?>) section.getChildren().get(0);
        final RobotCodeHoldingElement<?> codeHolder2 = (RobotCodeHoldingElement<?>) section.getChildren().get(1);

        final List<RobotKeywordCall> callsToRemove = newArrayList(codeHolder1.getChildren().get(1),
                codeHolder1.getChildren().get(3), codeHolder2.getChildren().get(0), codeHolder2.getChildren().get(2));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final DeleteKeywordCallCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteKeywordCallCommand(callsToRemove));
        command.execute();

        assertThat(codeHolder1.getChildren()).hasSize(3);
        assertThat(codeHolder1.getChildren().get(0).getName()).isEqualTo("Documentation");
        assertThat(codeHolder1.getChildren().get(1).getName()).isEqualTo("Teardown");
        assertThat(codeHolder1.getChildren().get(2).getName()).isEqualTo("Log");
        assertThat(codeHolder2.getChildren()).hasSize(2);
        assertThat(codeHolder2.getChildren().get(0).getName()).isEqualTo("Timeout");
        assertThat(codeHolder2.getChildren().get(1).getName()).isEqualTo("Log");

        List<EditorCommand> undoCommands = command.getUndoCommands();
        for (final EditorCommand undoCommand : undoCommands) {
            undoCommand.execute();
        }

        assertThat(codeHolder1.getChildren()).hasSize(5);
        assertThat(codeHolder1.getChildren().get(0).getName()).isEqualTo("Documentation");
        assertThat(codeHolder1.getChildren().get(1).getName()).isEqualTo("Tags");
        assertThat(codeHolder1.getChildren().get(2).getName()).isEqualTo("Teardown");
        assertThat(codeHolder1.getChildren().get(3).getName()).isEqualTo("Log");
        assertThat(codeHolder2.getChildren()).hasSize(4);
        assertThat(codeHolder2.getChildren().get(1).getName()).isEqualTo("Timeout");
        assertThat(codeHolder2.getChildren().get(3).getName()).isEqualTo("Log");

        final List<EditorCommand> redoCommands = new ArrayList<>();
        for (final EditorCommand undoCommand : undoCommands) {
            redoCommands.addAll(0, undoCommand.getUndoCommands());
        }
        for (final EditorCommand redoCommand : redoCommands) {
            redoCommand.execute();
        }

        assertThat(codeHolder1.getChildren()).hasSize(3);
        assertThat(codeHolder1.getChildren().get(0).getName()).isEqualTo("Documentation");
        assertThat(codeHolder1.getChildren().get(1).getName()).isEqualTo("Teardown");
        assertThat(codeHolder1.getChildren().get(2).getName()).isEqualTo("Log");
        assertThat(codeHolder2.getChildren()).hasSize(2);
        assertThat(codeHolder2.getChildren().get(0).getName()).isEqualTo("Timeout");
        assertThat(codeHolder2.getChildren().get(1).getName()).isEqualTo("Log");
        
        undoCommands = new ArrayList<>();
        for (final EditorCommand redoCommand : redoCommands) {
            undoCommands.addAll(0, redoCommand.getUndoCommands());
        }
        for (final EditorCommand undoCommand : undoCommands) {
            undoCommand.execute();
        }
        
        assertThat(codeHolder1.getChildren()).hasSize(5);
        assertThat(codeHolder1.getChildren().get(0).getName()).isEqualTo("Documentation");
        assertThat(codeHolder1.getChildren().get(1).getName()).isEqualTo("Tags");
        assertThat(codeHolder1.getChildren().get(2).getName()).isEqualTo("Teardown");
        assertThat(codeHolder1.getChildren().get(3).getName()).isEqualTo("Log");
        assertThat(codeHolder2.getChildren()).hasSize(4);
        assertThat(codeHolder2.getChildren().get(1).getName()).isEqualTo("Timeout");
        assertThat(codeHolder2.getChildren().get(3).getName()).isEqualTo("Log");
    }

    private static RobotSuiteFile createModel() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  [Documentation]  doc")
                .appendLine("  [Tags]  a  b")
                .appendLine("  [Teardown]    1    # comment    abc")
                .appendLine("  Log  10")
                .appendLine("  Log  20")
                .appendLine("case 2")
                .appendLine("  [Setup]  Log  xxx")
                .appendLine("  [Timeout]    10s    # comment")
                .appendLine("  [unknown]    1    # comment")
                .appendLine("  Log  10")
                .appendLine("*** Keywords ***")
                .appendLine("keyword 1")
                .appendLine("  [Documentation]  doc")
                .appendLine("  [Tags]  a  b")
                .appendLine("  [Teardown]    1    # comment    abc")
                .appendLine("  Log  10")
                .appendLine("  Log  20")
                .appendLine("keyword 2")
                .appendLine("  [Arguments]  ${x}   ${y}")
                .appendLine("  [Timeout]    10s    # comment")
                .appendLine("  [unknown]    1    # comment")
                .appendLine("  Log  10")
                .build();
        return model;
    }
}
