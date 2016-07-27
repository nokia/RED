package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCaseConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

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

        assertThat(section.getChildren().size()).isEqualTo(3);
        assertThat(section.getChildren().get(0).getName()).isEqualTo("case 1");
        assertThat(section.getChildren().get(1).getName()).isEqualTo("case 2");
        assertThat(section.getChildren().get(2).getName()).isEqualTo("case 3");

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

        assertThat(section.getChildren().size()).isEqualTo(3);
        assertThat(section.getChildren().get(0).getName()).isEqualTo("case 1");
        assertThat(section.getChildren().get(0)).has(RobotCaseConditions.properlySetParent());
        assertThat(section.getChildren().get(1).getName()).isEqualTo("case 3");
        assertThat(section.getChildren().get(1)).has(RobotCaseConditions.properlySetParent());
        assertThat(section.getChildren().get(2).getName()).isEqualTo("case 2");
        assertThat(section.getChildren().get(2)).has(RobotCaseConditions.properlySetParent());

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_CASE_MOVED, section);
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
