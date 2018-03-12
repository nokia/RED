/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelFunctions.toNames;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
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
        assertThat(transform(secondCase.getChildren(), toNames())).containsExactly("Setup", "Log");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(secondCase.getChildren(), toNames())).containsExactly("Setup", "Log");

        verifyZeroInteractions(eventBroker);
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
        assertThat(transform(secondKeyword.getChildren(), toNames())).containsExactly("Teardown", "Log");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(secondKeyword.getChildren(), toNames())).containsExactly("Teardown", "Log");

        verifyZeroInteractions(eventBroker);
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
        assertThat(transform(firstCase.getChildren(), toNames())).containsExactly("Log1", "Log2", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(firstCase.getChildren(), toNames())).containsExactly("Log1", "Log2", "Log3");

        verifyZeroInteractions(eventBroker);
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
        assertThat(transform(firstKeyword.getChildren(), toNames())).containsExactly("Log1", "Log2", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(firstKeyword.getChildren(), toNames())).containsExactly("Log1", "Log2", "Log3");

        verifyZeroInteractions(eventBroker);
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
        assertThat(transform(firstCase.getChildren(), toNames())).containsExactly("Tags", "Log1", "Documentation",
                "Log2", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(firstCase.getChildren(), toNames())).containsExactly("Tags", "Documentation", "Log1",
                "Log2", "Log3");

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
        assertThat(transform(firstKeyword.getChildren(), toNames())).containsExactly("Tags", "Log1", "Documentation",
                "Log2", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(firstKeyword.getChildren(), toNames())).containsExactly("Tags", "Documentation", "Log1",
                "Log2", "Log3");

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
        assertThat(transform(firstCase.getChildren(), toNames())).containsExactly("Documentation", "Tags", "Log1",
                "Log2", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(firstCase.getChildren(), toNames())).containsExactly("Tags", "Documentation", "Log1",
                "Log2", "Log3");

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
        assertThat(transform(firstKeyword.getChildren(), toNames())).containsExactly("Documentation", "Tags", "Log1",
                "Log2", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(firstKeyword.getChildren(), toNames())).containsExactly("Tags", "Documentation", "Log1",
                "Log2", "Log3");

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
        assertThat(transform(firstCase.getChildren(), toNames())).containsExactly("Log2", "Log1", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(firstCase.getChildren(), toNames())).containsExactly("Log1", "Log2", "Log3");

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
        assertThat(transform(firstKeyword.getChildren(), toNames())).containsExactly("Log2", "Log1", "Log3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(firstKeyword.getChildren(), toNames())).containsExactly("Log1", "Log2", "Log3");

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

        assertThat(transform(fstCase.getChildren(), toNames())).containsExactly("Log1", "Log2", "Log3");
        assertThat(transform(sndCase.getChildren(), toNames())).containsExactly("Log");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(fstCase.getChildren(), toNames())).containsExactly("Log1", "Log2", "Log3");
        assertThat(transform(sndCase.getChildren(), toNames())).containsExactly("Log");

        verifyZeroInteractions(eventBroker);
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

        assertThat(transform(fstKeyword.getChildren(), toNames())).containsExactly("Log1", "Log2", "Log3");
        assertThat(transform(sndKeyword.getChildren(), toNames())).containsExactly("Log");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(fstKeyword.getChildren(), toNames())).containsExactly("Log1", "Log2", "Log3");
        assertThat(transform(sndKeyword.getChildren(), toNames())).containsExactly("Log");

        verifyZeroInteractions(eventBroker);
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
