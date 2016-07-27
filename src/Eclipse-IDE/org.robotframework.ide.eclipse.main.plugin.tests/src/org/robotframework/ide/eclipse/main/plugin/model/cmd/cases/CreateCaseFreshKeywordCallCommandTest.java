package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCallConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class CreateCaseFreshKeywordCallCommandTest {

    @Test
    public void whenCommandIsUsedWithoutIndex_newCallIsProperlyAddedAtTheEnd() {
        final RobotCase robotCase = createTestCase();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final CreateCaseFreshKeywordCallCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateCaseFreshKeywordCallCommand(robotCase));
        command.execute();

        assertThat(robotCase.getChildren().size()).isEqualTo(4);

        final RobotKeywordCall addedCall = robotCase.getChildren().get(3);
        assertThat(addedCall.getName()).isEqualTo("");
        assertThat(addedCall.getArguments()).isEmpty();
        assertThat(addedCall.getChildren()).isEmpty();
        assertThat(addedCall).has(RobotKeywordCallConditions.properlySetParent());

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, robotCase);
    }

    @Test
    public void whenCommandIsUsedWithIndex_newCaseIsProperlyAddedAtSpecifiedPlace() {
        final RobotCase robotCase = createTestCase();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final CreateCaseFreshKeywordCallCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateCaseFreshKeywordCallCommand(robotCase, 1));
        command.execute();

        assertThat(robotCase.getChildren().size()).isEqualTo(4);

        final RobotKeywordCall addedCall = robotCase.getChildren().get(1);
        assertThat(addedCall.getName()).isEqualTo("");
        assertThat(addedCall.getArguments()).isEmpty();
        assertThat(addedCall).has(RobotKeywordCallConditions.properlySetParent());

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, robotCase);
    }

    private static RobotCase createTestCase() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  Log  10")
                .appendLine("  Log  20")
                .appendLine("  Log  30")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section.getChildren().get(0);
    }
}
