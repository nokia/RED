/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.RedClipboardMock;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;

public class PasteTasksCellsCommandsCollectorTest {

    private final PasteTasksCellsCommandsCollector collector = new PasteTasksCellsCommandsCollector();


    @Test
    public void thereAreNoCodeHoldersInClipboard_whenCasesAreInClipboard() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotCase robotTest = new RobotCase(null, test);
        final Object content = new RobotCase[] { robotTest };

        final RedClipboard clipboard = new RedClipboardMock();
        clipboard.insertContent(content);

        assertThat(collector.hasCodeHolders(clipboard)).isFalse();
        assertThat(collector.getCodeHolders(clipboard)).isNull();
    }

    @Test
    public void thereAreNoCodeHoldersInClipboard_whenKeywordDefsAreInClipboard() {
        final UserKeyword keyword = new UserKeyword(RobotToken.create("keyword"));
        final RobotKeywordDefinition robotKeyword = new RobotKeywordDefinition(null, keyword);
        final Object content = new RobotKeywordDefinition[] { robotKeyword };

        final RedClipboard clipboard = new RedClipboardMock();
        clipboard.insertContent(content);

        assertThat(collector.hasCodeHolders(clipboard)).isFalse();
        assertThat(collector.getCodeHolders(clipboard)).isNull();
    }

    @Test
    public void thereAreCodeHoldersInClipboard_whenTasksAreInClipboard() {
        final Task task = new Task(RobotToken.create("task"));
        final RobotTask robotTask = new RobotTask(null, task);
        final Object content = new RobotTask[] { robotTask };

        final RedClipboard clipboard = new RedClipboardMock();
        clipboard.insertContent(content);

        assertThat(collector.hasCodeHolders(clipboard)).isTrue();
        assertThat(collector.getCodeHolders(clipboard)).containsExactly(robotTask);
    }

    @Test
    public void onlyNameIsReturnedAsValuesOfTask_whenInFirstColumn() {
        final Task task = new Task(RobotToken.create("task"));
        final RobotTask robotTask = new RobotTask(null, task);

        assertThat(collector.getValuesFromCodeHolder(robotTask, 0, 10)).containsExactly("task");
        for (int i = 1; i < 10; i++) {
            assertThat(collector.getValuesFromCodeHolder(robotTask, i, 10)).isEmpty();
        }
    }

}
