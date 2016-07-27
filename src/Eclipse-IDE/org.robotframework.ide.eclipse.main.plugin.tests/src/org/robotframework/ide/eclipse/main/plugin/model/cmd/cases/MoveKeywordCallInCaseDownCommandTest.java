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

public class MoveKeywordCallInCaseDownCommandTest {

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToMoveSetting() {
        final List<RobotCase> cases = createTestCasesWithSettings();
        final RobotKeywordCall setting = cases.get(0).getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallInCaseDownCommand(setting))
                .execute();
    }

    @Test
    public void nothingHappens_whenTryingToMoveExecutableWhichIsBottommost() {
        final List<RobotCase> cases = createTestCasesWithoutSettings();
        final RobotCase lastCase = cases.get(2);
        final RobotKeywordCall call = lastCase.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallInCaseDownCommand(call))
                .execute();

        assertThat(lastCase.getChildren().get(0).getName()).isEqualTo("Log1");
        assertThat(lastCase.getChildren().get(1).getName()).isEqualTo("Log2");

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void rowIsProperlyMovedDown_whenMovingExecutableWhichHasExecutableAfter() {
        final List<RobotCase> cases = createTestCasesWithoutSettings();
        final RobotCase firstCase = cases.get(0);
        final RobotKeywordCall call = firstCase.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallInCaseDownCommand(call))
                .execute();

        assertThat(firstCase.getChildren().get(0).getName()).isEqualTo("Log1");
        assertThat(firstCase.getChildren().get(1).getName()).isEqualTo("Log3");
        assertThat(firstCase.getChildren().get(2).getName()).isEqualTo("Log2");

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, firstCase);
    }

    @Test
    public void rowIsProperlyMovedToNextCase_whenItIsBottommostInCase() {
        final List<RobotCase> cases = createTestCasesWithoutSettings();
        final RobotCase sndCase = cases.get(1);
        final RobotCase trdCase = cases.get(2);
        final RobotKeywordCall callToMove = sndCase.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallInCaseDownCommand(callToMove))
                .execute();

        assertThat(sndCase.getChildren()).isEmpty();

        assertThat(trdCase.getChildren()).hasSize(3);
        assertThat(trdCase.getChildren().get(0).getName()).isEqualTo("Log");
        assertThat(trdCase.getChildren().get(1).getName()).isEqualTo("Log1");
        assertThat(trdCase.getChildren().get(2).getName()).isEqualTo("Log2");

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, trdCase);
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, sndCase);
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, trdCase);
    }

    @Test
    public void rowIsProperlyMovedToNextCaseWithSettings_whenItIsBottomostInCase() {
        final List<RobotCase> cases = createTestCasesWithSettings();
        final RobotCase fstCase = cases.get(0);
        final RobotCase sndCase = cases.get(1);
        final RobotKeywordCall callToMove = fstCase.getChildren().get(3);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallInCaseDownCommand(callToMove))
                .execute();

        assertThat(fstCase.getChildren()).hasSize(3);
        assertThat(fstCase.getChildren().get(0).getName()).isEqualTo("Tags");
        assertThat(fstCase.getChildren().get(1).getName()).isEqualTo("Log1");
        assertThat(fstCase.getChildren().get(2).getName()).isEqualTo("Log2");

        assertThat(sndCase.getChildren()).hasSize(3);
        assertThat(sndCase.getChildren().get(0).getName()).isEqualTo("Setup");
        assertThat(sndCase.getChildren().get(1).getName()).isEqualTo("Log3");
        assertThat(sndCase.getChildren().get(2).getName()).isEqualTo("Log");

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, sndCase);
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, fstCase);
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, sndCase);
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
                .appendLine("  Log1  30")
                .appendLine("  Log2  30")
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
                .appendLine("  Log1  30")
                .appendLine("  Log2  30")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section.getChildren();
    }
}
