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

public class MoveKeywordCallUpCommandTest {

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToMoveSettingOfTestCase() {
        final List<RobotCase> cases = createTestCasesWithSettings();
        final RobotKeywordCall setting = cases.get(0).getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(setting))
                .execute();
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToMoveSettingOfKeyword() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithSettings();
        final RobotKeywordCall setting = keywords.get(0).getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(setting))
                .execute();
    }

    @Test
    public void nothingHappens_whenTryingToMoveExecutableWhichIsTopmostInTestCase() {
        final List<RobotCase> cases = createTestCasesWithoutSettings();
        final RobotCase firstCase = cases.get(0);
        final RobotKeywordCall call = firstCase.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call))
                .execute();

        assertThat(firstCase.getChildren().get(0).getName()).isEqualTo("Log1");

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void nothingHappens_whenTryingToMoveExecutableWhichIsTopmostInKeyword() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithoutSettings();
        final RobotKeywordDefinition firstKeyword = keywords.get(0);
        final RobotKeywordCall call = firstKeyword.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call))
                .execute();

        assertThat(firstKeyword.getChildren().get(0).getName()).isEqualTo("Log1");

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void nothingHappens_whenTryingToMoveExecutableWhichIsTopmostExcludingSettingsInCase() {
        final List<RobotCase> cases = createTestCasesWithSettings();
        final RobotCase firstCase = cases.get(0);
        final RobotKeywordCall call = firstCase.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call))
                .execute();

        assertThat(firstCase.getChildren().get(0).getName()).isEqualTo("Tags");
        assertThat(firstCase.getChildren().get(1).getName()).isEqualTo("Log1");

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void nothingHappens_whenTryingToMoveExecutableWhichIsTopmostExcludingSettingsInKeyword() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithSettings();
        final RobotKeywordDefinition firstKeyword = keywords.get(0);
        final RobotKeywordCall call = firstKeyword.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call))
                .execute();

        assertThat(firstKeyword.getChildren().get(0).getName()).isEqualTo("Tags");
        assertThat(firstKeyword.getChildren().get(1).getName()).isEqualTo("Log1");

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void rowIsProperlyMovedUp_whenMovingExecutableWhichHasExecutableBeforeInTestCase() {
        final List<RobotCase> cases = createTestCasesWithoutSettings();
        final RobotCase firstCase = cases.get(0);
        final RobotKeywordCall call = firstCase.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call))
                .execute();

        assertThat(firstCase.getChildren().get(0).getName()).isEqualTo("Log2");
        assertThat(firstCase.getChildren().get(1).getName()).isEqualTo("Log1");
        assertThat(firstCase.getChildren().get(2).getName()).isEqualTo("Log3");

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, firstCase);
    }

    @Test
    public void rowIsProperlyMovedUp_whenMovingExecutableWhichHasExecutableBeforeInKeyword() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithoutSettings();
        final RobotKeywordDefinition firstKeyword = keywords.get(0);
        final RobotKeywordCall call = firstKeyword.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(call))
                .execute();

        assertThat(firstKeyword.getChildren().get(0).getName()).isEqualTo("Log2");
        assertThat(firstKeyword.getChildren().get(1).getName()).isEqualTo("Log1");
        assertThat(firstKeyword.getChildren().get(2).getName()).isEqualTo("Log3");

        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, firstKeyword);
    }

    @Test
    public void rowIsNotMovedToPreviousCase_whenItIsTopmostInNonFirstCase() {
        final List<RobotCase> cases = createTestCasesWithoutSettings();
        final RobotCase fstCase = cases.get(0);
        final RobotCase sndCase = cases.get(1);
        final RobotKeywordCall callToMove = sndCase.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(callToMove))
                .execute();

        assertThat(fstCase.getChildren()).hasSize(3);
        assertThat(fstCase.getChildren().get(0).getName()).isEqualTo("Log1");
        assertThat(fstCase.getChildren().get(1).getName()).isEqualTo("Log2");
        assertThat(fstCase.getChildren().get(2).getName()).isEqualTo("Log3");

        assertThat(sndCase.getChildren()).isNotEmpty();
        assertThat(sndCase.getChildren().get(0).getName()).isEqualTo("Log");
    }

    @Test
    public void rowIsNotMovedToPreviousCase_whenItIsTopmostInNonFirstKeyword() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithoutSettings();
        final RobotKeywordDefinition fstKeyword = keywords.get(0);
        final RobotKeywordDefinition sndKeyword = keywords.get(1);
        final RobotKeywordCall callToMove = sndKeyword.getChildren().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(callToMove))
                .execute();

        assertThat(fstKeyword.getChildren()).hasSize(3);
        assertThat(fstKeyword.getChildren().get(0).getName()).isEqualTo("Log1");
        assertThat(fstKeyword.getChildren().get(1).getName()).isEqualTo("Log2");
        assertThat(fstKeyword.getChildren().get(2).getName()).isEqualTo("Log3");

        assertThat(sndKeyword.getChildren()).isNotEmpty();
        assertThat(sndKeyword.getChildren().get(0).getName()).isEqualTo("Log");
    }

    @Test
    public void rowIsNotMovedToPreviousCase_whenItIsTopmostExcludingSettingsInNonFirstCase() {
        final List<RobotCase> cases = createTestCasesWithSettings();
        final RobotCase fstCase = cases.get(0);
        final RobotCase sndCase = cases.get(1);
        final RobotKeywordCall callToMove = sndCase.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(callToMove))
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
    public void rowIsNotMovedToPreviousKeyword_whenItIsTopmostExcludingSettingsInNonFirstKeyword() {
        final List<RobotKeywordDefinition> keywords = createKeywordsWithSettings();
        final RobotKeywordDefinition fstKeyword = keywords.get(0);
        final RobotKeywordDefinition sndKeyword = keywords.get(1);
        final RobotKeywordCall callToMove = sndKeyword.getChildren().get(1);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveKeywordCallUpCommand(callToMove))
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
