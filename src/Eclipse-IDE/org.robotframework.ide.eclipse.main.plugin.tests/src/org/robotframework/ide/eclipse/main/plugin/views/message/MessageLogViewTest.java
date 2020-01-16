/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;

@ExtendWith(FreshShellExtension.class)
public class MessageLogViewTest {

    @FreshShell
    Shell shell;

    @Test
    public void messageLogViewIsEmptyAfterConstruction_whenThereWereNoLaunchesSoFar() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final MessageLogView view = new MessageLogView(executionService);
        view.postConstruct(shell);

        assertThat(view.getTextControl().getText()).isEmpty();
    }

    @Test
    public void messageLogViewHasMessagesFromLastLaunchAfterConstruction_whenThereWereLaunches() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final RobotTestsLaunch launch1 = executionService.testExecutionStarting(null);
        final ExecutionMessagesStore store1 = new ExecutionMessagesStore();
        store1.open();
        launch1.getExecutionData(ExecutionMessagesStore.class, () -> store1);
        store1.append("message1\n");
        store1.append("message2\n");
        store1.close();
        executionService.testExecutionEnded(launch1);

        final RobotTestsLaunch launch2 = executionService.testExecutionStarting(null);
        final ExecutionMessagesStore store2 = new ExecutionMessagesStore();
        store2.open();
        launch2.getExecutionData(ExecutionMessagesStore.class, () -> store2);
        store2.append("message3\n");
        store2.append("message4\n");
        store2.close();
        executionService.testExecutionEnded(launch2);

        final MessageLogView view = new MessageLogView(executionService);
        view.postConstruct(shell);

        execAllAwaitingMessages();

        assertThat(view.getTextControl().getText()).isEqualTo("message3\nmessage4\n");
    }

    @Test
    public void messageLogViewIsCleared_whenItIsOpenedAndNewLaunchIsStarting() throws InterruptedException {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final RobotTestsLaunch launch = executionService.testExecutionStarting(null);
        final ExecutionMessagesStore store = new ExecutionMessagesStore();
        store.open();
        launch.getExecutionData(ExecutionMessagesStore.class, () -> store);
        store.append("message1\n");
        store.append("message2\n");
        store.close();
        executionService.testExecutionEnded(launch);

        // open view after first execution has ended
        final MessageLogView view = new MessageLogView(executionService);
        view.postConstruct(shell);

        execAllAwaitingMessages();
        assertThat(view.getTextControl().getText()).isEqualTo("message1\nmessage2\n");

        final Thread secondExecutionThread = new Thread(() -> {
            executionService.testExecutionStarting(null);
        });
        secondExecutionThread.start();

        waitForViewToContain(view, "");
    }

    @Test
    public void messageLogViewIsUpdated_whenMessagesAreAppendedDuringExecution() throws Exception {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final MessageLogView view = new MessageLogView(executionService);
        view.postConstruct(shell);

        final Thread executionThread = new Thread(() -> {
            final RobotTestsLaunch launch = executionService.testExecutionStarting(null);
            final ExecutionMessagesStore store = launch.getExecutionData(ExecutionMessagesStore.class).get();
            store.open();
            store.append("message1\n");
            store.append("message2\n");
        });
        executionThread.start();

        waitForViewToContain(view, "message1\nmessage2\n");
    }

    @Test
    public void messageLogViewDoesNotWrapWords_whenInitialized() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final MessageLogView view = new MessageLogView(executionService);
        view.postConstruct(shell);

        assertThat(view.getTextControl().getWordWrap()).isFalse();
    }

    @Test
    public void messageLogProperlyWrapWords_whenToggledOnce() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final MessageLogView view = new MessageLogView(executionService);
        view.postConstruct(shell);

        view.toggleWordsWrapping();

        assertThat(view.getTextControl().getWordWrap()).isTrue();
    }

    @Test
    public void messageLogDoesNotWrapWords_whenToggledTwice() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final MessageLogView view = new MessageLogView(executionService);
        view.postConstruct(shell);

        view.toggleWordsWrapping();
        view.toggleWordsWrapping();

        assertThat(view.getTextControl().getWordWrap()).isFalse();
    }

    private void waitForViewToContain(final MessageLogView view, final String expectedText)
            throws InterruptedException {
        final int timeoutInMs = 5000;
        final int sleepTimeInMs = 200;

        int trial = 0;
        while (trial < timeoutInMs / sleepTimeInMs) {
            if (view.getTextControl().getText().equals(expectedText)) {
                return;
            }
            trial++;
            Thread.sleep(sleepTimeInMs);
            execAllAwaitingMessages();
        }
        assertThat(view.getTextControl().getText()).isEqualTo(expectedText);
    }

    private void execAllAwaitingMessages() {
        while (Display.getDefault().readAndDispatch()) {
            // handle all events coming to UI
        }
    }
}
