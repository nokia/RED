/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelFunctions.toNames;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCaseConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveCaseDownCommandTest {

    @Test
    public void nothingHappens_whenTryingToMoveCaseWhichIsAlreadyTheLastOne() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase caseToMove = section.getChildren().get(2);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveCaseDownCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveCaseDownCommand(caseToMove));
        command.execute();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("case 1", "case 2", "case 3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), toNames())).containsExactly("case 1", "case 2", "case 3");

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void caseIsProperlyMovedUp_whenTryingToMoveNonFirstCase() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase caseToMove = section.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final MoveCaseDownCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveCaseDownCommand(caseToMove));
        command.execute();

        assertThat(transform(section.getChildren(), toNames())).containsExactly("case 1", "case 3", "case 2");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), toNames())).containsExactly("case 1", "case 2", "case 3");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent());

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_CASE_MOVED, section);
        verifyNoMoreInteractions(eventBroker);
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
}
