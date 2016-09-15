package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.childrens;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.name;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCaseConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.collect.ImmutableMap;

public class InsertCasesCommandTest {

    @Test
    public void nothingHappens_whenThereAreNoCasesToInsert() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase[] casesToInsert = new RobotCase[0];

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final InsertCasesCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertCasesCommand(section, casesToInsert));
        command.execute();
        assertThat(section.getChildren().size()).isEqualTo(3);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren().size()).isEqualTo(3);

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void caseAreProperlInsertedAtTheSectionEnd_whenNoIndexIsProvided() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase[] casesToInsert = createCasesToInsert();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final InsertCasesCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertCasesCommand(section, casesToInsert));
        command.execute();

        assertThat(section.getChildren().size()).isEqualTo(5);

        assertCase(section.getChildren().get(0), "case 1");
        assertCase(section.getChildren().get(1), "case 2");
        assertCase(section.getChildren().get(2), "case 3");
        assertCase(section.getChildren().get(3), "inserted case 1");
        assertCase(section.getChildren().get(4), "inserted case 2");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(section.getChildren().size()).isEqualTo(3);

        assertCase(section.getChildren().get(0), "case 1");
        assertCase(section.getChildren().get(1), "case 2");
        assertCase(section.getChildren().get(2), "case 3");

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_CASE_ADDED), eq(
                ImmutableMap.<String, Object> of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA,
                        newArrayList(casesToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_CASE_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void caseAreProperlInsertedInsideTheSection_whenIndexIsProvided() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase[] casesToInsert = createCasesToInsert();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final InsertCasesCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new InsertCasesCommand(section, 1, casesToInsert));
        command.execute();

        assertThat(section.getChildren().size()).isEqualTo(5);

        assertCase(section.getChildren().get(0), "case 1");
        assertCase(section.getChildren().get(1), "inserted case 1");
        assertCase(section.getChildren().get(2), "inserted case 2");
        assertCase(section.getChildren().get(3), "case 2");
        assertCase(section.getChildren().get(4), "case 3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(section.getChildren().size()).isEqualTo(3);

        assertCase(section.getChildren().get(0), "case 1");
        assertCase(section.getChildren().get(1), "case 2");
        assertCase(section.getChildren().get(2), "case 3");

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_CASE_ADDED), eq(
                ImmutableMap.<String, Object> of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA,
                        newArrayList(casesToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_CASE_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    private static void assertCase(final RobotCase testCase, final String expectedName) {
        assertThat(testCase).has(RobotCaseConditions.properlySetParent()).has(name(expectedName)).has(childrens());
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

    private static RobotCase[] createCasesToInsert() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("inserted case 1")
                .appendLine("  Log  10")
                .appendLine("inserted case 2")
                .appendLine("  Log  20")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section.getChildren().toArray(new RobotCase[0]);
    }
}
