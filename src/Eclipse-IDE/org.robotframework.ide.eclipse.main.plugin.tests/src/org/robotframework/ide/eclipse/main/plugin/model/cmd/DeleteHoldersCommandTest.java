package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.children;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.name;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCaseConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinitionConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTaskConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.collect.ImmutableMap;

public class DeleteHoldersCommandTest {

    private IEventBroker eventBroker;

    @Before
    public void beforeTest() {
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void nothingHappens_whenThereAreNoCasesToRemove() {
        final RobotCasesSection section = createTestCasesSection();
        final List<RobotCase> casesToRemove = newArrayList();

        final DeleteHoldersCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteHoldersCommand(casesToRemove));
        command.execute();
        assertThat(section.getChildren().size()).isEqualTo(3);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren().size()).isEqualTo(3);

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void singleCaseIsProperlyRemoved() {
        final RobotCasesSection section = createTestCasesSection();
        final List<RobotCase> casesToRemove = newArrayList(section.getChildren().get(1));

        final DeleteHoldersCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteHoldersCommand(casesToRemove));
        command.execute();

        assertThat(section.getChildren()).hasSize(2);
        assertThat(section.getChildren().get(0)).has(RobotCaseConditions.properlySetParent())
                .has(name("case 1"))
                .has(children());
        assertThat(section.getChildren().get(1)).has(RobotCaseConditions.properlySetParent())
                .has(name("case 3"))
                .has(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(section.getChildren()).hasSize(3);
        assertThat(section.getChildren().get(1)).has(RobotCaseConditions.properlySetParent())
                .has(name("case 2"))
                .has(children());

        verify(eventBroker).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, casesToRemove)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void multipleCasesAreProperlyRemoved() {
        final RobotCasesSection section = createTestCasesSection();
        final List<RobotCase> casesToRemove = newArrayList(section.getChildren().get(0), section.getChildren().get(2));

        final DeleteHoldersCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteHoldersCommand(casesToRemove));
        command.execute();

        assertThat(section.getChildren()).hasSize(1);
        assertThat(section.getChildren().get(0)).has(RobotCaseConditions.properlySetParent())
                .has(name("case 2"))
                .has(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(section.getChildren()).hasSize(3);
        assertThat(section.getChildren().get(0)).has(RobotCaseConditions.properlySetParent())
                .has(name("case 1"))
                .has(children());
        assertThat(section.getChildren().get(1)).has(RobotCaseConditions.properlySetParent())
                .has(name("case 2"))
                .has(children());
        assertThat(section.getChildren().get(2)).has(RobotCaseConditions.properlySetParent())
                .has(name("case 3"))
                .has(children());

        verify(eventBroker).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED), eq(ImmutableMap.of(IEventBroker.DATA,
                section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(section.getChildren().get(0)))));
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED), eq(ImmutableMap.of(IEventBroker.DATA,
                section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(section.getChildren().get(2)))));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void nothingHappens_whenThereAreNoKeywordsToRemove() {
        final RobotKeywordsSection section = createKeywordsSection();
        final List<RobotKeywordDefinition> keywordsToRemove = newArrayList();

        final DeleteHoldersCommand command = new DeleteHoldersCommand(keywordsToRemove);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), RobotElement::getName)).containsExactly("kw 1", "kw 2", "kw 3");

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), RobotElement::getName)).containsExactly("kw 1", "kw 2", "kw 3");

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void singleKeywordIsProperlyRemoved() {
        final RobotKeywordsSection section = createKeywordsSection();
        final List<RobotKeywordDefinition> keywordsToRemove = newArrayList(section.getChildren().get(1));

        final DeleteHoldersCommand command = new DeleteHoldersCommand(keywordsToRemove);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), RobotElement::getName)).containsExactly("kw 1", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), RobotElement::getName)).containsExactly("kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        verify(eventBroker).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, keywordsToRemove)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void multipleKeywordsAreProperlyRemoved() {
        final RobotKeywordsSection section = createKeywordsSection();
        final List<RobotKeywordDefinition> keywordsToRemove = newArrayList(section.getChildren().get(0),
                section.getChildren().get(2));

        final DeleteHoldersCommand command = new DeleteHoldersCommand(keywordsToRemove);
        command.setEventBroker(eventBroker);

        command.execute();
        assertThat(transform(section.getChildren(), RobotElement::getName)).containsExactly("kw 2");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(transform(section.getChildren(), RobotElement::getName)).containsExactly("kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(RobotKeywordDefinitionConditions.properlySetParent()).have(children());

        verify(eventBroker).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA,
                        newArrayList(section.getChildren().get(0)))));
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA,
                        newArrayList(section.getChildren().get(2)))));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void nothingHappens_whenThereAreNoTasksToRemove() {
        final RobotTasksSection section = createTasksSection();
        final List<RobotCase> casesToRemove = newArrayList();

        final DeleteHoldersCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteHoldersCommand(casesToRemove));
        command.execute();
        assertThat(section.getChildren().size()).isEqualTo(3);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(section.getChildren().size()).isEqualTo(3);

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void singleTaskIsProperlyRemoved() {
        final RobotTasksSection section = createTasksSection();
        final List<RobotTask> tasksToRemove = newArrayList(section.getChildren().get(1));

        final DeleteHoldersCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteHoldersCommand(tasksToRemove));
        command.execute();

        assertThat(section.getChildren()).hasSize(2);
        assertThat(section.getChildren().get(0)).has(RobotTaskConditions.properlySetParent())
                .has(name("task 1"))
                .has(children());
        assertThat(section.getChildren().get(1)).has(RobotTaskConditions.properlySetParent())
                .has(name("task 3"))
                .has(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(section.getChildren()).hasSize(3);
        assertThat(section.getChildren().get(1)).has(RobotTaskConditions.properlySetParent())
                .has(name("task 2"))
                .has(children());

        verify(eventBroker).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED),
                eq(ImmutableMap.of(IEventBroker.DATA, section, RobotModelEvents.ADDITIONAL_DATA, tasksToRemove)));
        verifyNoMoreInteractions(eventBroker);
    }

    @Test
    public void multipleTasksAreProperlyRemoved() {
        final RobotTasksSection section = createTasksSection();
        final List<RobotTask> casesToRemove = newArrayList(section.getChildren().get(0), section.getChildren().get(2));

        final DeleteHoldersCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new DeleteHoldersCommand(casesToRemove));
        command.execute();

        assertThat(section.getChildren()).hasSize(1);
        assertThat(section.getChildren().get(0)).has(RobotTaskConditions.properlySetParent())
                .has(name("task 2"))
                .has(children());

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(section.getChildren()).hasSize(3);
        assertThat(section.getChildren().get(0)).has(RobotTaskConditions.properlySetParent())
                .has(name("task 1"))
                .has(children());
        assertThat(section.getChildren().get(1)).has(RobotTaskConditions.properlySetParent())
                .has(name("task 2"))
                .has(children());
        assertThat(section.getChildren().get(2)).has(RobotTaskConditions.properlySetParent())
                .has(name("task 3"))
                .has(children());

        verify(eventBroker).send(RobotModelEvents.ROBOT_ELEMENT_REMOVED, section);
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED), eq(ImmutableMap.of(IEventBroker.DATA,
                section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(section.getChildren().get(0)))));
        verify(eventBroker).send(eq(RobotModelEvents.ROBOT_ELEMENT_ADDED), eq(ImmutableMap.of(IEventBroker.DATA,
                section, RobotModelEvents.ADDITIONAL_DATA, newArrayList(section.getChildren().get(2)))));
        verifyNoMoreInteractions(eventBroker);
    }

    private static RobotCasesSection createTestCasesSection() {
        return new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  [Tags]  a  b")
                .appendLine("  Log  10")
                .appendLine("case 2")
                .appendLine("  [Setup]  Log  xxx")
                .appendLine("  Log  10")
                .appendLine("case 3")
                .appendLine("  Log  10")
                .build()
                .findSection(RobotCasesSection.class)
                .get();
    }

    private static RobotTasksSection createTasksSection() {
        return new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Tasks ***")
                .appendLine("task 1")
                .appendLine("  [Tags]  a  b")
                .appendLine("  Log  10")
                .appendLine("task 2")
                .appendLine("  [Setup]  Log  xxx")
                .appendLine("  Log  10")
                .appendLine("task 3")
                .appendLine("  Log  10")
                .build()
                .findSection(RobotTasksSection.class)
                .get();
    }

    private static RobotKeywordsSection createKeywordsSection() {
        return new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw 1")
                .appendLine("  [Tags]  a  b")
                .appendLine("  Log  10")
                .appendLine("kw 2")
                .appendLine("  [Teardown]  Log  xxx")
                .appendLine("  Log  10")
                .appendLine("kw 3")
                .appendLine("  Log  10")
                .build()
                .findSection(RobotKeywordsSection.class)
                .get();
    }
}
