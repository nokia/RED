/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.children;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.RedClipboardMock;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCallConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTaskConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler.PasteTasksHandler.E4PasteTasksHandler;

public class PasteTasksHandlerTest {

    private final E4PasteTasksHandler handler = new E4PasteTasksHandler();

    private final RedClipboard clipboard = new RedClipboardMock();

    private final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();

    @BeforeEach
    public void beforeTest() {
        clipboard.clear();
        commandsStack.clear();
    }

    @Test
    public void actualHandlerUsesProperE4Handler() {
        final PasteTasksHandler handler = new PasteTasksHandler();

        assertThat(handler).extracting("component").isInstanceOf(E4PasteTasksHandler.class);
    }

    @Test
    public void whenNoTasksSectionExists_itIsCreatedForPastedTasks() {
        final RobotTask[] tasks = createTasksToPaste();
        clipboard.insertContent((Object) tasks);

        final RobotSuiteFile emptyModel = new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1)).build();
        handler.pasteTasks(emptyModel, selection(), clipboard, commandsStack);

        assertThat(emptyModel.getSections()).hasSize(1);
        final RobotTasksSection section = emptyModel.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("task 1", "task 2", "task 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotTaskConditions.properlySetParent());
    }

    @Test
    public void whenSelectionIsEmpty_tasksArePastedAtTheEndOfSection() {
        final RobotTask[] tasks = createTasksToPaste();
        clipboard.insertContent((Object) tasks);

        final RobotSuiteFile model = createTargetModel();
        handler.pasteTasks(model, selection(), clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotTasksSection section = model.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("existing task 1", "existing task 2", "existing task 3", "task 1", "task 2", "task 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotTaskConditions.properlySetParent());
    }

    @Test
    public void whenNonNestedAddingTokenIsSelected_tasksArePastedAtTheEndOfSection() {
        final RobotTask[] tasks = createTasksToPaste();
        clipboard.insertContent((Object) tasks);

        final RobotSuiteFile model = createTargetModel();
        final IStructuredSelection selection = selection(new AddingToken(null, mock(TokenState.class)));
        handler.pasteTasks(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotTasksSection section = model.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("existing task 1", "existing task 2", "existing task 3", "task 1", "task 2", "task 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotTaskConditions.properlySetParent());
    }

    @Test
    public void whenTaskIsSelected_tasksArePastedBeforeIt() {
        final RobotTask[] tasks = createTasksToPaste();
        clipboard.insertContent((Object) tasks);

        final RobotSuiteFile model = createTargetModel();
        final RobotTask selectedTask = model.findSection(RobotTasksSection.class).get().getChildren().get(1);

        final IStructuredSelection selection = selection(selectedTask);
        handler.pasteTasks(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotTasksSection section = model.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("existing task 1", "task 1", "task 2", "task 3", "existing task 2", "existing task 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotTaskConditions.properlySetParent());
    }

    @Test
    public void whenKeywordCallIsSelected_tasksArePastedBeforeItsParent() {
        final RobotTask[] tasks = createTasksToPaste();
        clipboard.insertContent((Object) tasks);

        final RobotSuiteFile model = createTargetModel();
        final RobotTask parentTask = model.findSection(RobotTasksSection.class).get().getChildren().get(1);
        final RobotKeywordCall selectedCall = parentTask.getChildren().get(1);

        final IStructuredSelection selection = selection(selectedCall);
        handler.pasteTasks(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotTasksSection section = model.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("existing task 1", "task 1", "task 2", "task 3", "existing task 2", "existing task 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotTaskConditions.properlySetParent());
    }

    @Test
    public void whenNestedAddingTokenIsSelected_tasksArePastedBeforeItsParent() {
        final RobotTask[] cases = createTasksToPaste();
        clipboard.insertContent((Object) cases);

        final RobotSuiteFile model = createTargetModel();
        final RobotTask tokenParent = model.findSection(RobotTasksSection.class).get().getChildren().get(1);

        final IStructuredSelection selection = selection(new AddingToken(tokenParent, mock(TokenState.class)));
        handler.pasteTasks(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotTasksSection section = model.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("existing task 1", "task 1", "task 2", "task 3", "existing task 2", "existing task 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotTaskConditions.properlySetParent());
    }

    @Test
    public void whenNoTasksSectionExists_noCallsArePastedAndNothingChanges() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile emptyModel = new RobotSuiteFileCreator().build();

        handler.pasteTasks(emptyModel, selection(), clipboard, commandsStack);

        assertThat(emptyModel.getSections()).isEmpty();
    }

    @Test
    public void whenSelectionIsEmpty_noCallsArePastedAndNothingChanges() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile model = createTargetModel();

        handler.pasteTasks(model, selection(), clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotTasksSection section = model.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("existing task 1", "existing task 2", "existing task 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotTaskConditions.properlySetParent());
    }

    @Test
    public void whenNonNestedAddingTokenIsSelected_noCallsArePastedAndNothingChanges() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile model = createTargetModel();

        final IStructuredSelection selection = selection(new AddingToken(null, mock(TokenState.class)));
        handler.pasteTasks(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotTasksSection section = model.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("existing task 1", "existing task 2", "existing task 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotTaskConditions.properlySetParent());
    }

    @Test
    public void whenCaseIsSelected_callsArePastedAtTheEndOfIt() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile model = createTargetModel();
        final RobotTask selectedTask = model.findSection(RobotTasksSection.class).get().getChildren().get(1);

        final IStructuredSelection selection = selection(selectedTask);
        handler.pasteTasks(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotTasksSection section = model.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("existing task 1", "existing task 2", "existing task 3");

        final RobotTask fstTask = section.getChildren().get(0);
        assertThat(fstTask).has(RobotTaskConditions.properlySetParent());
        assertThat(fstTask.getChildren()).extracting(RobotElement::getName).containsExactly("a", "b");
        assertThat(fstTask.getChildren()).have(RobotKeywordCallConditions.properlySetParent());

        final RobotTask sndTask = section.getChildren().get(1);
        assertThat(sndTask).has(RobotTaskConditions.properlySetParent());
        assertThat(sndTask.getChildren()).extracting(RobotElement::getName).containsExactly("c", "d", "call1", "call2");
        assertThat(sndTask.getChildren()).have(RobotKeywordCallConditions.properlySetParent());

        final RobotTask trdTask = section.getChildren().get(2);
        assertThat(trdTask).has(RobotTaskConditions.properlySetParent());
        assertThat(trdTask.getChildren()).extracting(RobotElement::getName).containsExactly("e", "f");
        assertThat(trdTask.getChildren()).have(RobotKeywordCallConditions.properlySetParent());
    }

    @Test
    public void whenKeywordCallIsSelected_callsArePastedBeforeIt() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile model = createTargetModel();
        final RobotTask parentCase = model.findSection(RobotTasksSection.class).get().getChildren().get(1);
        final RobotKeywordCall selectedCall = parentCase.getChildren().get(1);

        final IStructuredSelection selection = selection(selectedCall);
        handler.pasteTasks(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotTasksSection section = model.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("existing task 1", "existing task 2", "existing task 3");

        final RobotTask fstTask = section.getChildren().get(0);
        assertThat(fstTask).has(RobotTaskConditions.properlySetParent());
        assertThat(fstTask.getChildren()).extracting(RobotElement::getName).containsExactly("a", "b");
        assertThat(fstTask.getChildren()).have(RobotKeywordCallConditions.properlySetParent());

        final RobotTask sndTask = section.getChildren().get(1);
        assertThat(sndTask).has(RobotTaskConditions.properlySetParent());
        assertThat(sndTask.getChildren()).extracting(RobotElement::getName).containsExactly("c", "call1", "call2", "d");
        assertThat(sndTask.getChildren()).have(RobotKeywordCallConditions.properlySetParent());

        final RobotTask trdTask = section.getChildren().get(2);
        assertThat(trdTask).has(RobotTaskConditions.properlySetParent());
        assertThat(trdTask.getChildren()).extracting(RobotElement::getName).containsExactly("e", "f");
        assertThat(trdTask.getChildren()).have(RobotKeywordCallConditions.properlySetParent());
    }

    @Test
    public void whenNestedAddingTokenIsSelected_callsArePastedAtTheEndOfParentTask() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile model = createTargetModel();
        final RobotTask parentTask = model.findSection(RobotTasksSection.class).get().getChildren().get(1);

        final IStructuredSelection selection = selection(new AddingToken(parentTask, mock(TokenState.class)));
        handler.pasteTasks(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotTasksSection section = model.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("existing task 1", "existing task 2", "existing task 3");

        final RobotTask fstTask = section.getChildren().get(0);
        assertThat(fstTask).has(RobotTaskConditions.properlySetParent());
        assertThat(fstTask.getChildren()).extracting(RobotElement::getName).containsExactly("a", "b");
        assertThat(fstTask.getChildren()).have(RobotKeywordCallConditions.properlySetParent());

        final RobotTask sndTask = section.getChildren().get(1);
        assertThat(sndTask).has(RobotTaskConditions.properlySetParent());
        assertThat(sndTask.getChildren()).extracting(RobotElement::getName).containsExactly("c", "d", "call1", "call2");
        assertThat(sndTask.getChildren()).have(RobotKeywordCallConditions.properlySetParent());

        final RobotTask trdTask = section.getChildren().get(2);
        assertThat(trdTask).has(RobotTaskConditions.properlySetParent());
        assertThat(trdTask.getChildren()).extracting(RobotElement::getName).containsExactly("e", "f");
        assertThat(trdTask.getChildren()).have(RobotKeywordCallConditions.properlySetParent());
    }

    @Test
    public void whenThereAreTestTasksAndCallsInClipboard_onlyTasksAreInserted() {
        final RobotTask[] tasks = createTasksToPaste();
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent(tasks, calls);

        final RobotSuiteFile model = createTargetModel();
        handler.pasteTasks(model, selection(), clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotTasksSection section = model.findSection(RobotTasksSection.class).get();
        assertThat(section.getChildren()).extracting(RobotElement::getName)
                .containsExactly("existing task 1", "existing task 2", "existing task 3", "task 1", "task 2", "task 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotTaskConditions.properlySetParent());
    }

    private static IStructuredSelection selection(final Object... selectedObjects) {
        return new StructuredSelection(selectedObjects);
    }

    private static RobotSuiteFile createTargetModel() {
        return new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Tasks ***")
                .appendLine("existing task 1")
                .appendLine("  a  1  2")
                .appendLine("  b  3  4")
                .appendLine("existing task 2")
                .appendLine("  c  5  6")
                .appendLine("  d  7  8")
                .appendLine("existing task 3")
                .appendLine("  e  9  10")
                .appendLine("  f  11  12")
                .build();
    }

    private static RobotTask[] createTasksToPaste() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Tasks ***")
                .appendLine("task 1")
                .appendLine("  a  1  2")
                .appendLine("  b  3  4")
                .appendLine("task 2")
                .appendLine("  c  5  6")
                .appendLine("  d  7  8")
                .appendLine("task 3")
                .appendLine("  e  9  10")
                .appendLine("  f  11  12")
                .build();
        return model.findSection(RobotTasksSection.class).get().getChildren().toArray(new RobotTask[0]);
    }

    private static RobotKeywordCall[] createCallsToPaste() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Tasks ***")
                .appendLine("task x")
                .appendLine("  call1  1  2")
                .appendLine("task y")
                .appendLine("  call2  3  4")
                .build();
        final RobotTasksSection section = model.findSection(RobotTasksSection.class).get();
        final List<RobotKeywordCall> calls = new ArrayList<>();
        calls.addAll(section.getChildren().get(0).getChildren());
        calls.addAll(section.getChildren().get(1).getChildren());
        return calls.toArray(new RobotKeywordCall[0]);
    }
}
