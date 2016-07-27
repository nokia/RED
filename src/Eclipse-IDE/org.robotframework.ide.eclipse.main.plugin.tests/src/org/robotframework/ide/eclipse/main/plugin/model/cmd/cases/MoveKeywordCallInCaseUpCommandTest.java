package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

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

public class MoveKeywordCallInCaseUpCommandTest {

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToMoveSetting() {
        final List<RobotCase> cases = createTestCasesWithSettings();
        final RobotKeywordCall setting = cases.get(0).getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallInCaseUpCommand(setting))
                .execute();
    }

    @Test
    public void nothingHappens_whenTryingToMoveExecutableWhichIsTopmost() {
        final List<RobotCase> cases = createTestCasesWithoutSettings();
        final RobotCase firstCase = cases.get(0);
        final RobotKeywordCall call = firstCase.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallInCaseUpCommand(call))
                .execute();

        assertThat(firstCase.getChildren().get(0).getName()).isEqualTo("Log1");

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void nothingHappens_whenTryingToMoveExecutableWhichIsTopmostExcludingSettings() {
        final List<RobotCase> cases = createTestCasesWithSettings();
        final RobotCase firstCase = cases.get(0);
        final RobotKeywordCall call = firstCase.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallInCaseUpCommand(call))
                .execute();

        assertThat(firstCase.getChildren().get(0).getName()).isEqualTo("Tags");
        assertThat(firstCase.getChildren().get(1).getName()).isEqualTo("Log1");

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void rowIsProperlyMovedUp_whenMovingExecutableWhichHasExecutableBefore() {
        final List<RobotCase> cases = createTestCasesWithoutSettings();
        final RobotCase firstCase = cases.get(0);
        final RobotKeywordCall call = firstCase.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallInCaseUpCommand(call))
                .execute();

        assertThat(firstCase.getChildren().get(0).getName()).isEqualTo("Log2");
        assertThat(firstCase.getChildren().get(1).getName()).isEqualTo("Log1");
        assertThat(firstCase.getChildren().get(2).getName()).isEqualTo("Log3");

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, firstCase);
    }

    @Test
    public void rowIsProperlyMovedToPreviousCase_whenItIsTopmostInNonFirstCase() {
        final List<RobotCase> cases = createTestCasesWithoutSettings();
        final RobotCase fstCase = cases.get(0);
        final RobotCase sndCase = cases.get(1);
        final RobotKeywordCall callToMove = sndCase.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallInCaseUpCommand(callToMove))
                .execute();

        assertThat(fstCase.getChildren()).hasSize(4);
        assertThat(fstCase.getChildren().get(0).getName()).isEqualTo("Log1");
        assertThat(fstCase.getChildren().get(1).getName()).isEqualTo("Log2");
        assertThat(fstCase.getChildren().get(2).getName()).isEqualTo("Log3");
        assertThat(fstCase.getChildren().get(3).getName()).isEqualTo("Log");

        assertThat(sndCase.getChildren()).isEmpty();

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, fstCase);
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, sndCase);
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, fstCase);
    }

    @Test
    public void rowIsProperlyMovedToPreviousCase_whenItIsTopmostExcludingSettingsInNonFirstCase() {
        final List<RobotCase> cases = createTestCasesWithSettings();
        final RobotCase fstCase = cases.get(0);
        final RobotCase sndCase = cases.get(1);
        final RobotKeywordCall callToMove = sndCase.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallInCaseUpCommand(callToMove))
                .execute();

        assertThat(fstCase.getChildren()).hasSize(5);
        assertThat(fstCase.getChildren().get(0).getName()).isEqualTo("Tags");
        assertThat(fstCase.getChildren().get(1).getName()).isEqualTo("Log1");
        assertThat(fstCase.getChildren().get(2).getName()).isEqualTo("Log2");
        assertThat(fstCase.getChildren().get(3).getName()).isEqualTo("Log3");
        assertThat(fstCase.getChildren().get(4).getName()).isEqualTo("Log");

        assertThat(sndCase.getChildren()).hasSize(1);
        assertThat(sndCase.getChildren().get(0).getName()).isEqualTo("Setup");

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, fstCase);
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, sndCase);
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, fstCase);
    }

    private static List<RobotCase> createTestCasesWithSettings() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  [Tags]  a  b")
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
}
