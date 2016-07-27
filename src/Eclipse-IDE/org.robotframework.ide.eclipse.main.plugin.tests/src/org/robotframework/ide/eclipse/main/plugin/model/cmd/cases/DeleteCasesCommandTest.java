package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCaseConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class DeleteCasesCommandTest {

    @Test
    public void nothingHappens_whenThereAreNoCasesToRemove() {
        final RobotCasesSection section = createTestCasesSection();
        final List<RobotCase> casesToRemove = newArrayList();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final DeleteCasesCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteCasesCommand(casesToRemove));
        command.execute();

        assertThat(section.getChildren().size()).isEqualTo(3);

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void singleCaseIsProperlyRemoved() {
        final RobotCasesSection section = createTestCasesSection();
        final List<RobotCase> casesToRemove = newArrayList(section.getChildren().get(1));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final DeleteCasesCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteCasesCommand(casesToRemove));
        command.execute();

        assertThat(section.getChildren().size()).isEqualTo(2);

        final RobotCase firstCase = section.getChildren().get(0);
        assertThat(firstCase.getName()).isEqualTo("case 1");
        assertThat(firstCase.getChildren()).isNotEmpty();
        assertThat(firstCase).has(RobotCaseConditions.properlySetParent());

        final RobotCase sndCase = section.getChildren().get(1);
        assertThat(sndCase.getName()).isEqualTo("case 3");
        assertThat(sndCase.getChildren()).isNotEmpty();
        assertThat(sndCase).has(RobotCaseConditions.properlySetParent());

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_CASE_REMOVED, section);
    }

    @Test
    public void multipleCasesAreProperlyRemoved() {
        final RobotCasesSection section = createTestCasesSection();
        final List<RobotCase> casesToRemove = newArrayList(section.getChildren().get(0), section.getChildren().get(2));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final DeleteCasesCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteCasesCommand(casesToRemove));
        command.execute();

        assertThat(section.getChildren().size()).isEqualTo(1);

        final RobotCase firstCase = section.getChildren().get(0);
        assertThat(firstCase.getName()).isEqualTo("case 2");
        assertThat(firstCase.getChildren()).isNotEmpty();
        assertThat(firstCase).has(RobotCaseConditions.properlySetParent());

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_CASE_REMOVED, section);
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
