package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.e4.core.services.events.IEventBroker;
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

import com.google.common.collect.ImmutableMap;

@RunWith(Theories.class)
public class CreateFreshKeywordCallCommandTest {

    @DataPoints
    public static RobotCodeHoldingElement<?>[] elements() {
        final RobotCodeHoldingElement<?>[] elements = new RobotCodeHoldingElement[2];
        elements[0] = createTestCase();
        elements[1] = createKeywordDefinition();
        return elements;
    }

    @Theory
    public void whenCommandIsUsedWithoutIndex_newCallIsProperlyAddedAtTheEnd(
            final RobotCodeHoldingElement<?> codeHolder) {

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final CreateFreshKeywordCallCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshKeywordCallCommand(codeHolder));
        command.execute();

        assertThat(codeHolder.getChildren().size()).isEqualTo(4);

        final RobotKeywordCall addedCall = codeHolder.getChildren().get(3);
        assertThat(addedCall.getName()).isEqualTo("");
        assertThat(addedCall.getArguments()).isEmpty();
        assertThat(addedCall.getChildren()).isEmpty();
        assertThat(addedCall).has(RobotKeywordCallConditions.properlySetParent());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(ImmutableMap
                .<String, Object> of(IEventBroker.DATA, codeHolder, RobotModelEvents.ADDITIONAL_DATA, addedCall)));
    }

    @Theory
    public void whenCommandIsUsedWithIndex_newCaseIsProperlyAddedAtSpecifiedPlace(
            final RobotCodeHoldingElement<?> codeHolder) {

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final CreateFreshKeywordCallCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new CreateFreshKeywordCallCommand(codeHolder, 1));
        command.execute();

        assertThat(codeHolder.getChildren().size()).isEqualTo(4);

        final RobotKeywordCall addedCall = codeHolder.getChildren().get(1);
        assertThat(addedCall.getName()).isEqualTo("");
        assertThat(addedCall.getArguments()).isEmpty();
        assertThat(addedCall).has(RobotKeywordCallConditions.properlySetParent());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED), eq(ImmutableMap
                .<String, Object> of(IEventBroker.DATA, codeHolder, RobotModelEvents.ADDITIONAL_DATA, addedCall)));
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

    private static RobotKeywordDefinition createKeywordDefinition() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  call  10")
                .appendLine("  call  20")
                .appendLine("  call  30")
                .build();
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        return section.getChildren().get(0);
    }
}
