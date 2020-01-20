/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveKeywordCallUpCommandTest {

    @Test
    public void nothingHappens_whenTryingToMoveSettingWhichIsTopmostInTestCase() {
        final List<RobotCase> cases = createTestCasesWithSettings();
        final RobotCase secondCase = cases.get(1);
        final RobotKeywordCall call = secondCase.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveKeywordCallUpCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call));
        command.execute();
        assertThat(secondCase.getChildren()).extracting(RobotElement::getName).containsExactly("Setup", "Log");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(secondCase.getChildren()).extracting(RobotElement::getName).containsExactly("Setup", "Log");

        verifyNoInteractions(eventBroker);
    }

    @Test
    public void nothingHappens_whenTryingToMoveSettingWhichIsTopmostInKeyword() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithSettings();
        final RobotKeywordDefinition secondKeyword = keywords.get(1);
        final RobotKeywordCall call = secondKeyword.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveKeywordCallUpCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call));
        command.execute();
        assertThat(secondKeyword.getChildren()).extracting(RobotElement::getName).containsExactly("Teardown", "Log");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(secondKeyword.getChildren()).extracting(RobotElement::getName).containsExactly("Teardown", "Log");

        verifyNoInteractions(eventBroker);
    }

    @Test
    public void nothingHappens_whenTryingToMoveExecutableWhichIsTopmostInTestCase() {
        final List<RobotCase> cases = createTestCasesWithoutSettings();
        final RobotCase firstCase = cases.get(0);
        final RobotKeywordCall call = firstCase.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveKeywordCallUpCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call));
        command.execute();
        assertThat(firstCase.getChildren()).extracting(RobotElement::getName).containsExactly("Log1", "Log2", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(firstCase.getChildren()).extracting(RobotElement::getName).containsExactly("Log1", "Log2", "Log3");

        verifyNoInteractions(eventBroker);
    }

    @Test
    public void nothingHappens_whenTryingToMoveExecutableWhichIsTopmostInKeyword() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithoutSettings();
        final RobotKeywordDefinition firstKeyword = keywords.get(0);
        final RobotKeywordCall call = firstKeyword.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveKeywordCallUpCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call));
        command.execute();
        assertThat(firstKeyword.getChildren()).extracting(RobotElement::getName)
                .containsExactly("Log1", "Log2", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(firstKeyword.getChildren()).extracting(RobotElement::getName)
                .containsExactly("Log1", "Log2", "Log3");

        verifyNoInteractions(eventBroker);
    }

    @Test
    public void rowIsProperlyMovedUp_whenMovingExecutableWhichHasSettingBeforeInCase() {
        final List<RobotCase> cases = createTestCasesWithSettings();
        final RobotCase firstCase = cases.get(0);
        final RobotKeywordCall call = firstCase.getChildren().get(2);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveKeywordCallUpCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call));
        command.execute();
        assertThat(firstCase.getChildren()).extracting(RobotElement::getName)
                .containsExactly("Tags", "Log1", "Documentation", "Log2", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(firstCase.getChildren()).extracting(RobotElement::getName)
                .containsExactly("Tags", "Documentation", "Log1", "Log2", "Log3");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, firstCase);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void rowIsProperlyMovedUp_whenMovingExecutableWhichHasSettingBeforeInKeyword() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithSettings();
        final RobotKeywordDefinition firstKeyword = keywords.get(0);
        final RobotKeywordCall call = firstKeyword.getChildren().get(2);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveKeywordCallUpCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call));
        command.execute();
        assertThat(firstKeyword.getChildren()).extracting(RobotElement::getName)
                .containsExactly("Tags", "Log1", "Documentation", "Log2", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(firstKeyword.getChildren()).extracting(RobotElement::getName)
                .containsExactly("Tags", "Documentation", "Log1", "Log2", "Log3");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, firstKeyword);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void rowIsProperlyMovedUp_whenMovingSettingWhichHasSettingBeforeInCase() {
        final List<RobotCase> cases = createTestCasesWithSettings();
        final RobotCase firstCase = cases.get(0);
        final RobotKeywordCall call = firstCase.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveKeywordCallUpCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call));
        command.execute();
        assertThat(firstCase.getChildren()).extracting(RobotElement::getName)
                .containsExactly("Documentation", "Tags", "Log1", "Log2", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(firstCase.getChildren()).extracting(RobotElement::getName)
                .containsExactly("Tags", "Documentation", "Log1", "Log2", "Log3");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, firstCase);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void rowIsProperlyMovedUp_whenMovingSettingWhichHasSettingBeforeInKeyword() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithSettings();
        final RobotKeywordDefinition firstKeyword = keywords.get(0);
        final RobotKeywordCall call = firstKeyword.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveKeywordCallUpCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call));
        command.execute();
        assertThat(firstKeyword.getChildren()).extracting(RobotElement::getName)
                .containsExactly("Documentation", "Tags", "Log1", "Log2", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(firstKeyword.getChildren()).extracting(RobotElement::getName)
                .containsExactly("Tags", "Documentation", "Log1", "Log2", "Log3");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, firstKeyword);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void rowIsProperlyMovedUp_whenMovingExecutableWhichHasExecutableBeforeInTestCase() {
        final List<RobotCase> cases = createTestCasesWithoutSettings();
        final RobotCase firstCase = cases.get(0);
        final RobotKeywordCall call = firstCase.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveKeywordCallUpCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call));
        command.execute();
        assertThat(firstCase.getChildren()).extracting(RobotElement::getName).containsExactly("Log2", "Log1", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(firstCase.getChildren()).extracting(RobotElement::getName).containsExactly("Log1", "Log2", "Log3");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, firstCase);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void rowIsProperlyMovedUp_whenMovingExecutableWhichHasExecutableBeforeInKeyword() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithoutSettings();
        final RobotKeywordDefinition firstKeyword = keywords.get(0);
        final RobotKeywordCall call = firstKeyword.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveKeywordCallUpCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call));
        command.execute();
        assertThat(firstKeyword.getChildren()).extracting(RobotElement::getName)
                .containsExactly("Log2", "Log1", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(firstKeyword.getChildren()).extracting(RobotElement::getName)
                .containsExactly("Log1", "Log2", "Log3");

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, firstKeyword);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void rowIsNotMovedToPreviousCase_whenItIsTopmostInNonFirstCase() {
        final List<RobotCase> cases = createTestCasesWithoutSettings();
        final RobotCase fstCase = cases.get(0);
        final RobotCase sndCase = cases.get(1);
        final RobotKeywordCall callToMove = sndCase.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveKeywordCallUpCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(callToMove));
        command.execute();

        assertThat(fstCase.getChildren()).extracting(RobotElement::getName).containsExactly("Log1", "Log2", "Log3");
        assertThat(sndCase.getChildren()).extracting(RobotElement::getName).containsExactly("Log");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(fstCase.getChildren()).extracting(RobotElement::getName).containsExactly("Log1", "Log2", "Log3");
        assertThat(sndCase.getChildren()).extracting(RobotElement::getName).containsExactly("Log");

        verifyNoInteractions(eventBroker);
    }

    @Test
    public void rowIsNotMovedToPreviousCase_whenItIsTopmostInNonFirstKeyword() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithoutSettings();
        final RobotKeywordDefinition fstKeyword = keywords.get(0);
        final RobotKeywordDefinition sndKeyword = keywords.get(1);
        final RobotKeywordCall callToMove = sndKeyword.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveKeywordCallUpCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(callToMove));
        command.execute();

        assertThat(fstKeyword.getChildren()).extracting(RobotElement::getName).containsExactly("Log1", "Log2", "Log3");
        assertThat(sndKeyword.getChildren()).extracting(RobotElement::getName).containsExactly("Log");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(fstKeyword.getChildren()).extracting(RobotElement::getName).containsExactly("Log1", "Log2", "Log3");
        assertThat(sndKeyword.getChildren()).extracting(RobotElement::getName).containsExactly("Log");

        verifyNoInteractions(eventBroker);
    }

    private static List<RobotCase> createTestCasesWithSettings() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  [Tags]  a  b")
                .appendLine("  [Documentation]  doc")
                .appendLine("  Log1  10")
                .appendLine("  Log2  10")
                .appendLine("  Log3  10")
                .appendLine("case 2")
                .appendLine("  [Setup]  Log  xxx")
                .appendLine("  Log  20")
                .appendLine("case 3")
                .appendLine("  Log  30")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section.getChildren();
    }

    private static List<RobotCase> createTestCasesWithoutSettings() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  Log1  10")
                .appendLine("  Log2  10")
                .appendLine("  Log3  10")
                .appendLine("case 2")
                .appendLine("  Log  20")
                .appendLine("case 3")
                .appendLine("  Log  30")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section.getChildren();
    }

    private static List<RobotKeywordDefinition> createKeywordsWithSettings() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword 1")
                .appendLine("  [Tags]  a  b")
                .appendLine("  [Documentation]  doc")
                .appendLine("  Log1  10")
                .appendLine("  Log2  10")
                .appendLine("  Log3  10")
                .appendLine("keyword 2")
                .appendLine("  [Teardown]  Log  xxx")
                .appendLine("  Log  20")
                .appendLine("keyword 3")
                .appendLine("  Log  30")
                .build();
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        return section.getChildren();
    }

    private static List<RobotKeywordDefinition> createKeywordsWithoutSettings() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword 1")
                .appendLine("  Log1  10")
                .appendLine("  Log2  10")
                .appendLine("  Log3  10")
                .appendLine("keyword 2")
                .appendLine("  Log  20")
                .appendLine("keyword 3")
                .appendLine("  Log  30")
                .build();
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        return section.getChildren();
    }
}
