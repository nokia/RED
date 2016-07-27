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
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class DeleteKeywordCallFromCasesCommandTest {

    @Test
    public void nothingHappens_whenThereAreNoCallsToRemove() {
        final RobotCasesSection section = createTestCasesSection();
        final List<RobotKeywordCall> callsToRemove = newArrayList();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final DeleteKeywordCallFromCasesCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteKeywordCallFromCasesCommand(callsToRemove));
        command.execute();

        assertThat(section.getChildren().size()).isEqualTo(2);

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void settingsAreProperlyRemoved_whenRemovingRowsFromSingleCase() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase case2 = section.getChildren().get(1);

        final List<RobotKeywordCall> callsToRemove = newArrayList(case2.getChildren().get(0),
                case2.getChildren().get(1), case2.getChildren().get(2));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final DeleteKeywordCallFromCasesCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteKeywordCallFromCasesCommand(callsToRemove));
        command.execute();

        assertThat(case2.getChildren().size()).isEqualTo(1);
        assertThat(case2.getChildren().get(0).getName()).isEqualTo("Log");
        assertThat(case2.getLinkedElement().getUnitSettings()).isEmpty();

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, case2);
    }

    @Test
    public void executableRowsAreProperlyRemoved_whenRemovingRowsFromSingleCase() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase case1 = section.getChildren().get(0);

        final List<RobotKeywordCall> callsToRemove = newArrayList(case1.getChildren().get(3),
                case1.getChildren().get(4));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final DeleteKeywordCallFromCasesCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteKeywordCallFromCasesCommand(callsToRemove));
        command.execute();

        assertThat(case1.getChildren().size()).isEqualTo(3);
        assertThat(case1.getChildren().get(0).getName()).isEqualTo("Documentation");
        assertThat(case1.getChildren().get(1).getName()).isEqualTo("Tags");
        assertThat(case1.getChildren().get(2).getName()).isEqualTo("Teardown");
        assertThat(case1.getLinkedElement().getExecutionContext()).isEmpty();

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, case1);
    }

    @Test
    public void rowsAreProperlyRemoved_whenRemovingFromDifferentCases() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase case1 = section.getChildren().get(0);
        final RobotCase case2 = section.getChildren().get(1);

        final List<RobotKeywordCall> callsToRemove = newArrayList(case1.getChildren().get(1),
                case1.getChildren().get(3), case2.getChildren().get(0), case2.getChildren().get(2));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final DeleteKeywordCallFromCasesCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteKeywordCallFromCasesCommand(callsToRemove));
        command.execute();

        assertThat(case1.getChildren().size()).isEqualTo(3);
        assertThat(case1.getChildren().get(0).getName()).isEqualTo("Documentation");
        assertThat(case1.getChildren().get(1).getName()).isEqualTo("Teardown");
        assertThat(case1.getChildren().get(2).getName()).isEqualTo("Log");
        assertThat(case1.getLinkedElement().getExecutionContext()).hasSize(1);
        assertThat(case1.getLinkedElement().getTags()).isEmpty();

        assertThat(case2.getChildren().size()).isEqualTo(2);
        assertThat(case2.getChildren().get(0).getName()).isEqualTo("Timeout");
        assertThat(case2.getChildren().get(1).getName()).isEqualTo("Log");
        assertThat(case2.getLinkedElement().getSetups()).isEmpty();
        assertThat(case2.getLinkedElement().getUnknownSettings()).isEmpty();

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, case1);
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, case2);
    }

    private static RobotCasesSection createTestCasesSection() {
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
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section;
    }
}
