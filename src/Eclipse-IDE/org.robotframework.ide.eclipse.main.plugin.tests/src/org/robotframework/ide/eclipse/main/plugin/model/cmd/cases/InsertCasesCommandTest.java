package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.children;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelFunctions.toNames;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCaseConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.collect.ImmutableMap;

public class InsertCasesCommandTest {

    private IEventBroker eventBroker;

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void nothingHappens_whenThereAreNoCasesToInsert() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase[] casesToInsert = new RobotCase[0];

        final InsertCasesCommand command = new InsertCasesCommand(section, casesToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(section.getChildren()).hasSize(3);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren()).hasSize(3);

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void caseAreProperlyInsertedAtTheSectionEnd_whenNoIndexIsProvided() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase[] casesToInsert = createCasesToInsert();

        final InsertCasesCommand command = new InsertCasesCommand(section, casesToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("case 1", "case 2", "case 3",
                "inserted case 1", "inserted case 2");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), toNames())).containsExactly("case 1", "case 2", "case 3");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent()).have(children());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_CASE_ADDED), eq(ImmutableMap.of(IEventBroker.DATA,
                section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(casesToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_CASE_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void caseAreProperlyInsertedInsideTheSection_whenIndexIsProvided() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase[] casesToInsert = createCasesToInsert();

        final InsertCasesCommand command = new InsertCasesCommand(section, 1, casesToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("case 1", "inserted case 1",
                "inserted case 2", "case 2", "case 3");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), toNames())).containsExactly("case 1", "case 2", "case 3");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent()).have(children());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_CASE_ADDED), eq(ImmutableMap.of(IEventBroker.DATA,
                section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(casesToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_CASE_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void casesAreProperlyInsertedAndRenamed_whenThereIsACaseWithSameNameAlready() {
        final RobotCasesSection section = createTestCasesSection();
        final RobotCase[] casesToInsert = createCasesWithSameNameToInsert();

        final InsertCasesCommand command = new InsertCasesCommand(section, casesToInsert);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("case 1", "case 2", "case 3", "case 4");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), toNames())).containsExactly("case 1", "case 2", "case 3");
        assertThat(section.getChildren()).have(RobotCaseConditions.properlySetParent()).have(children());

        verify(eventBroker, times(1)).send(eq(RobotModelEvents.ROBOT_CASE_ADDED), eq(ImmutableMap.of(IEventBroker.DATA,
                section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(casesToInsert))));
        verify(eventBroker, times(1)).send(RobotModelEvents.ROBOT_CASE_REMOVED, section);
        verifyNoMoreInteractions(eventBroker);
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

    private static RobotCase[] createCasesWithSameNameToInsert() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  Log  10")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section.getChildren().toArray(new RobotCase[0]);
    }
}
