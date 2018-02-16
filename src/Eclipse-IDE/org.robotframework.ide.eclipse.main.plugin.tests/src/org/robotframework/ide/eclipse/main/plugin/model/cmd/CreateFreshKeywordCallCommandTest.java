package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.name;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.noChildren;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCallConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.collect.ImmutableMap;

@RunWith(Theories.class)
public class CreateFreshKeywordCallCommandTest {

    private IEventBroker eventBroker;

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @DataPoints
    public static RobotCodeHoldingElement<?>[] elements() {
        return new RobotCodeHoldingElement[] { createTestCase(), createTestCaseWithSettings(), createKeywords(),
                createKeywordsWithSettings() };
    }

    @DataPoints
    public static int[] indexes() {
        return new int[] { 0, 1, 2, 3, 4, 5, 6 };
    }

    @Theory
    public void whenCommandIsUsedWithoutIndex_newCallIsProperlyAddedAtTheEnd(
            final RobotCodeHoldingElement<?> codeHolder) {

        final int oldSize = codeHolder.getChildren().size();

        final CreateFreshKeywordCallCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshKeywordCallCommand(codeHolder));
        command.execute();

        assertThat(codeHolder.getChildren().size()).isEqualTo(oldSize + 1);

        final RobotKeywordCall addedCall = codeHolder.getChildren().get(oldSize);
        assertThat(addedCall).has(RobotKeywordCallConditions.properlySetParent()).has(name("")).has(noChildren());
        assertThat(addedCall.getArguments()).isEmpty();

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(codeHolder.getChildren().size()).isEqualTo(oldSize);

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, codeHolder, RobotModelEvents.ADDITIONAL_DATA, addedCall)));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, codeHolder);
        verifyNoMoreInteractions(eventBroker);
    }

    @Theory
    public void whenCommandIsUsedWithIndex_newCallIsProperlyAddedAtSpecifiedPlace(
            final RobotCodeHoldingElement<?> codeHolder, final int index) {

        assumeTrue(index >= 0 && index <= codeHolder.getChildren().size());

        final int oldSize = codeHolder.getChildren().size();

        final CreateFreshKeywordCallCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshKeywordCallCommand(codeHolder, index));
        command.execute();

        assertThat(codeHolder.getChildren().size()).isEqualTo(oldSize + 1);

        final RobotKeywordCall addedCall = codeHolder.getChildren().get(index);
        assertThat(addedCall).has(RobotKeywordCallConditions.properlySetParent()).has(name(""));
        assertThat(addedCall.getArguments()).isEmpty();

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(codeHolder.getChildren().size()).isEqualTo(oldSize);

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, codeHolder, RobotModelEvents.ADDITIONAL_DATA, addedCall)));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, codeHolder);
        verifyNoMoreInteractions(eventBroker);
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

    private static RobotCase createTestCaseWithSettings() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  [setup]  a")
                .appendLine("  [tags]  a")
                .appendLine("  [teardown]  a")
                .appendLine("  Log  10")
                .appendLine("  Log  20")
                .appendLine("  Log  30")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section.getChildren().get(0);
    }

    private static RobotKeywordDefinition createKeywords() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  call  10")
                .appendLine("  call  20")
                .appendLine("  call  30")
                .build();
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        return section.getChildren().get(0);
    }

    private static RobotKeywordDefinition createKeywordsWithSettings() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [arguments]  a")
                .appendLine("  [tags]  a")
                .appendLine("  [teardown]  a")
                .appendLine("  call  10")
                .appendLine("  call  20")
                .appendLine("  call  30")
                .build();
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        return section.getChildren().get(0);
    }
}
