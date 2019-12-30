/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCaseConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinitionConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;


public class MoveCodeHolderUpCommandTest {

    @Test
    public void nothingHappens_whenTryingToMoveCaseWhichIsAlreadyTheFirstOne() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase caseToMove = section.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveCodeHolderUpCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveCodeHolderUpCommand(caseToMove));
        command.execute();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("case 1", "case 2", "case 3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("case 1", "case 2", "case 3");

        verifyNoInteractions(eventBroker);
    }

    @Test
    public void caseIsProperlyMovedUp_whenTryingToMoveNonFirstCase() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase caseToMove = section.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveCodeHolderUpCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveCodeHolderUpCommand(caseToMove));
        command.execute();

        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("case 2", "case 1", "case 3");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("case 1", "case 2", "case 3");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent());

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_ELEMENT_MOVED, section);
    }

    @Test
    public void nothingHappens_whenTryingToMoveKeywordWhichIsAlreadyTheFirstOne() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition keywordToMove = section.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveCodeHolderUpCommand command = new MoveCodeHolderUpCommand(keywordToMove);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(section.getChildren()).extracting(RobotElement::getName).containsExactly("kw 1", "kw 2", "kw 3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).extracting(RobotElement::getName).containsExactly("kw 1", "kw 2", "kw 3");

        verifyNoInteractions(eventBroker);
    }

    @Test
    public void keywordIsProperlyMovedUp_whenTryingToMoveNonFirstKeyword() {
        final RobotKeywordsSection section = createKeywordsSection();
        final RobotKeywordDefinition keywordToMove = section.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveCodeHolderUpCommand command = new MoveCodeHolderUpCommand(keywordToMove);
        command.setEventBroker(eventBroker);
        command.execute();

        assertThat(section.getChildren()).extracting(RobotElement::getName).containsExactly("kw 2", "kw 1", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).extracting(RobotElement::getName).containsExactly("kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent());

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_ELEMENT_MOVED, section);
    }

    private static RobotCasesSection createTestCasesSection() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  [Tags]  a  b")
                .appendLine("  Log  10")
                .appendLine("case 2")
                .appendLine("  [Setup]  Log  xxx")
                .appendLine("  Log  10")
                .appendLine("case 3")
                .appendLine("  Log  10")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section;
    }

    private static RobotKeywordsSection createKeywordsSection() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw 1")
                .appendLine("  [Tags]  a  b")
                .appendLine("  Log  10")
                .appendLine("kw 2")
                .appendLine("  [Teardown]  Log  xxx")
                .appendLine("  Log  10")
                .appendLine("kw 3")
                .appendLine("  Log  10")
                .build();
        return model.findSection(RobotKeywordsSection.class).get();
    }
}
