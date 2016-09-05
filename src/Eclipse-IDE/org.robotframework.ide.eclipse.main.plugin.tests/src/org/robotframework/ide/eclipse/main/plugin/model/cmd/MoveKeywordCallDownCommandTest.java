package org.robotframework.ide.eclipse.main.plugin.model.cmd;

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
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class MoveKeywordCallDownCommandTest {

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToMoveSettingOfTestCase() {
        final List<RobotCase> cases = createTestCasesWithSettings();
        final RobotKeywordCall setting = cases.get(0).getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallDownCommand(setting))
                .execute();
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToMoveSettingOfKeyword() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithSettings();
        final RobotKeywordCall setting = keywords.get(0).getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallDownCommand(setting))
                .execute();
    }

    @Test
    public void nothingHappens_whenTryingToMoveExecutableWhichIsBottommostInCases() {
        final List<RobotCase> cases = createTestCasesWithoutSettings();
        final RobotCase lastCase = cases.get(2);
        final RobotKeywordCall call = lastCase.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallDownCommand(call))
                .execute();

        assertThat(lastCase.getChildren().get(0).getName()).isEqualTo("Log1");
        assertThat(lastCase.getChildren().get(1).getName()).isEqualTo("Log2");

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void nothingHappens_whenTryingToMoveExecutableWhichIsBottommostInKeywords() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithoutSettings();
        final RobotKeywordDefinition lastKeyword = keywords.get(2);
        final RobotKeywordCall call = lastKeyword.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallDownCommand(call))
                .execute();

        assertThat(lastKeyword.getChildren().get(0).getName()).isEqualTo("Log1");
        assertThat(lastKeyword.getChildren().get(1).getName()).isEqualTo("Log2");

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void rowIsProperlyMovedDown_whenMovingExecutableWhichHasExecutableAfterInsideCase() {
        final List<RobotCase> cases = createTestCasesWithoutSettings();
        final RobotCase firstCase = cases.get(0);
        final RobotKeywordCall call = firstCase.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallDownCommand(call))
                .execute();

        assertThat(firstCase.getChildren().get(0).getName()).isEqualTo("Log1");
        assertThat(firstCase.getChildren().get(1).getName()).isEqualTo("Log3");
        assertThat(firstCase.getChildren().get(2).getName()).isEqualTo("Log2");

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, firstCase);
    }

    @Test
    public void rowIsProperlyMovedDown_whenMovingExecutableWhichHasExecutableAfterInsideKeyword() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithoutSettings();
        final RobotKeywordDefinition firstKeyword = keywords.get(0);
        final RobotKeywordCall call = firstKeyword.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallDownCommand(call))
                .execute();

        assertThat(firstKeyword.getChildren().get(0).getName()).isEqualTo("Log1");
        assertThat(firstKeyword.getChildren().get(1).getName()).isEqualTo("Log3");
        assertThat(firstKeyword.getChildren().get(2).getName()).isEqualTo("Log2");

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, firstKeyword);
    }

    @Test
    public void rowIsNotMovedToNextCase_whenItIsBottommostInCase() {
        final List<RobotCase> cases = createTestCasesWithoutSettings();
        final RobotCase sndCase = cases.get(1);
        final RobotCase trdCase = cases.get(2);
        final RobotKeywordCall callToMove = sndCase.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallDownCommand(callToMove))
                .execute();

        assertThat(sndCase.getChildren()).hasSize(1);
        assertThat(sndCase.getChildren().get(0).getName()).isEqualTo("Log");

        assertThat(trdCase.getChildren()).hasSize(2);
        assertThat(trdCase.getChildren().get(0).getName()).isEqualTo("Log1");
        assertThat(trdCase.getChildren().get(1).getName()).isEqualTo("Log2");
    }

    @Test
    public void rowIsNotMovedToNextKeyword_whenItIsBottommostInKeyword() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithoutSettings();
        final RobotKeywordDefinition sndKeyword = keywords.get(1);
        final RobotKeywordDefinition trdKeyword = keywords.get(2);
        final RobotKeywordCall callToMove = sndKeyword.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallDownCommand(callToMove))
                .execute();

        assertThat(sndKeyword.getChildren()).hasSize(1);
        assertThat(sndKeyword.getChildren().get(0).getName()).isEqualTo("Log");

        assertThat(trdKeyword.getChildren()).hasSize(2);
        assertThat(trdKeyword.getChildren().get(0).getName()).isEqualTo("Log1");
        assertThat(trdKeyword.getChildren().get(1).getName()).isEqualTo("Log2");
    }

    @Test
    public void rowIsNotMovedToNextCaseWithSettings_whenItIsBottomostInCase() {
        final List<RobotCase> cases = createTestCasesWithSettings();
        final RobotCase fstCase = cases.get(0);
        final RobotCase sndCase = cases.get(1);
        final RobotKeywordCall callToMove = fstCase.getChildren().get(3);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallDownCommand(callToMove))
                .execute();

        assertThat(fstCase.getChildren()).hasSize(4);
        assertThat(fstCase.getChildren().get(0).getName()).isEqualTo("Tags");
        assertThat(fstCase.getChildren().get(1).getName()).isEqualTo("Log1");
        assertThat(fstCase.getChildren().get(2).getName()).isEqualTo("Log2");
        assertThat(fstCase.getChildren().get(3).getName()).isEqualTo("Log3");

        assertThat(sndCase.getChildren()).hasSize(2);
        assertThat(sndCase.getChildren().get(0).getName()).isEqualTo("Setup");
        assertThat(sndCase.getChildren().get(1).getName()).isEqualTo("Log");
    }

    @Test
    public void rowIsNotMovedToNextKeywordWithSettings_whenItIsBottomostInKeyword() {
        final List<RobotKeywordDefinition> cases = createKeywordsWithSettings();
        final RobotKeywordDefinition fstKeyword = cases.get(0);
        final RobotKeywordDefinition sndKeyword = cases.get(1);
        final RobotKeywordCall callToMove = fstKeyword.getChildren().get(3);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallDownCommand(callToMove))
                .execute();

        assertThat(fstKeyword.getChildren()).hasSize(4);
        assertThat(fstKeyword.getChildren().get(0).getName()).isEqualTo("Tags");
        assertThat(fstKeyword.getChildren().get(1).getName()).isEqualTo("Log1");
        assertThat(fstKeyword.getChildren().get(2).getName()).isEqualTo("Log2");
        assertThat(fstKeyword.getChildren().get(3).getName()).isEqualTo("Log3");

        assertThat(sndKeyword.getChildren()).hasSize(2);
        assertThat(sndKeyword.getChildren().get(0).getName()).isEqualTo("Teardown");
        assertThat(sndKeyword.getChildren().get(1).getName()).isEqualTo("Log");
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

    private static List<RobotKeywordDefinition> createKeywordsWithSettings() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword 1")
                .appendLine("  [Tags]  a  b")
                .appendLine("  Log1  10")
                .appendLine("  Log2  10")
                .appendLine("  Log3  10")
                .appendLine("keyword 2")
                .appendLine("  [Teardown]  Log  xxx")
                .appendLine("  Log  20")
                .appendLine("keyword 3")
                .appendLine("  Log1  30")
                .appendLine("  Log2  30")
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
                .appendLine("  Log1  30")
                .appendLine("  Log2  30")
                .build();
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        return section.getChildren();
    }
}
