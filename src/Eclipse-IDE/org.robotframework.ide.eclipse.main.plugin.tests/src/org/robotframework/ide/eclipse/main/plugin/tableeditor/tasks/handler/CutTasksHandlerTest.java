/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.name;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.noFilePositions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.nullParent;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.RedClipboardMock;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler.CutTasksHandler.E4CutTasksHandler;

public class CutTasksHandlerTest {

    private final E4CutTasksHandler handler = new E4CutTasksHandler();

    private RedClipboardMock clipboard;

    private final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();

    @BeforeEach
    public void beforeTest() {
        clipboard = new RedClipboardMock();
        commandsStack.clear();
    }

    @Test
    public void actualHandlerUsesProperE4Handler() {
        final CutTasksHandler handler = new CutTasksHandler();

        assertThat(handler).extracting("component").isInstanceOf(E4CutTasksHandler.class);
    }

    @Test
    public void nothingIsCut_whenNothingIsSelected() {
        final IStructuredSelection selection = new StructuredSelection(newArrayList());

        handler.cutTasks(selection, clipboard, commandsStack);
        assertThat(clipboard.isEmpty());
    }

    @Test
    public void tasksAreCut_whenOnlyTheyAreSelected() {
        final RobotTasksSection tasks = createTasksSection();
        final RobotTask selectedTask = tasks.getChildren().get(0);

        final IStructuredSelection selection = new StructuredSelection(newArrayList(selectedTask));

        handler.cutTasks(selection, clipboard, commandsStack);

        assertThat(clipboard.getTasks()).hasSize(1);
        assertThat(clipboard.getTasks()[0]).has(nullParent()).has(noFilePositions()).has(name("task 1"));
        assertThat(clipboard.hasKeywordCalls()).isFalse();

        assertThat(tasks.getChildren()).extracting(RobotElement::getName).containsExactly("task 2");
    }

    @Test
    public void callsAreCopied_whenOnlyTheyAreSelected() {
        final RobotTasksSection tasks = createTasksSection();
        final RobotKeywordCall selectedCall1 = tasks.getChildren().get(1).getChildren().get(0);
        final RobotKeywordCall selectedCall2 = tasks.getChildren().get(1).getChildren().get(2);

        final IStructuredSelection selection = new StructuredSelection(newArrayList(selectedCall1, selectedCall2));

        handler.cutTasks(selection, clipboard, commandsStack);

        assertThat(clipboard.hasTasks()).isFalse();
        assertThat(clipboard.getKeywordCalls()).hasSize(2);
        assertThat(clipboard.getKeywordCalls()[0]).has(nullParent()).has(noFilePositions()).has(name("tags"));
        assertThat(clipboard.getKeywordCalls()[1]).has(nullParent()).has(noFilePositions()).has(name("d"));

        assertThat(tasks.getChildren()).extracting(RobotElement::getName).containsExactly("task 1", "task 2");
        assertThat(tasks.getChildren().get(0).getChildren()).extracting(RobotElement::getName)
                .containsExactly("tags", "a", "b");
        assertThat(tasks.getChildren().get(1).getChildren())
                .extracting(RobotElement::getName)
                .containsExactly("c");
    }

    private static RobotTasksSection createTasksSection() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().setVersion(new RobotVersion(3, 1))
                .appendLine("*** Tasks ***")
                .appendLine("task 1")
                .appendLine("  [tags]  tag1")
                .appendLine("  a  1")
                .appendLine("  b  2")
                .appendLine("task 2")
                .appendLine("  [tags]  tag2")
                .appendLine("  c  3")
                .appendLine("  d  4")
                .build();
        return model.findSection(RobotTasksSection.class).get();
    }
}
