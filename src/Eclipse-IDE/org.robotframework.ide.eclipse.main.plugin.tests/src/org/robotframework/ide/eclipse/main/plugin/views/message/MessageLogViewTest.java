/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.red.junit.ShellProvider;

public class MessageLogViewTest {

    @Rule
    public final ShellProvider shellProvider = new ShellProvider();

    @Test
    public void messageLogViewIsEmptyAfterConstruction_whenThereWereNoLaunchesSoFar() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final MessageLogView view = new MessageLogView(executionService);
        view.postConstruct(shellProvider.getShell());

        assertThat(view.getTextControl().getText()).isEmpty();
    }

    @Test
    public void messageLogViewHasMessagesFromLastLaunchAfterConstruction_whenThereWereLaunches() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final RobotTestsLaunch launch1 = executionService.testExecutionStarting(null);
        final ExecutionMessagesStore store1 = new ExecutionMessagesStore();
        launch1.getExecutionData(ExecutionMessagesStore.class, () -> store1);
        store1.append("message1\n");
        store1.append("message2\n");

        final RobotTestsLaunch launch2 = executionService.testExecutionStarting(null);
        final ExecutionMessagesStore store2 = new ExecutionMessagesStore();
        launch2.getExecutionData(ExecutionMessagesStore.class, () -> store2);
        store2.append("message3\n");
        store2.append("message4\n");

        final MessageLogView view = new MessageLogView(executionService);
        view.postConstruct(shellProvider.getShell());

        assertThat(view.getTextControl().getText()).isEqualTo("message3\nmessage4\n");
    }

    @Test
    public void messageLogViewIsCleared_whenItIsOpenedAndNewLaunchIsStarting() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final RobotTestsLaunch launch = executionService.testExecutionStarting(null);
        final ExecutionMessagesStore store = new ExecutionMessagesStore();
        launch.getExecutionData(ExecutionMessagesStore.class, () -> store);
        store.append("message1\n");
        store.append("message2\n");

        final MessageLogView view = new MessageLogView(executionService);
        view.postConstruct(shellProvider.getShell());

        assertThat(view.getTextControl().getText()).isEqualTo("message1\nmessage2\n");

        executionService.testExecutionStarting(null);

        assertThat(view.getTextControl().getText()).isEmpty();
    }

    @Test
    public void messageLogViewIsUpdated_whenMessagesAreAppendedDuringExecution() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final MessageLogView view = new MessageLogView(executionService);
        view.postConstruct(shellProvider.getShell());

        final RobotTestsLaunch launch = executionService.testExecutionStarting(null);
        final ExecutionMessagesStore store = launch.getExecutionData(ExecutionMessagesStore.class).get();
        store.append("message1\n");
        store.append("message2\n");

        assertThat(view.getTextControl().getText()).isEqualTo("message1\nmessage2\n");

    }

    @Test
    public void messageLogViewDoesNotWrapWords_whenInitialized() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final MessageLogView view = new MessageLogView(executionService);
        view.postConstruct(shellProvider.getShell());

        assertThat(view.getTextControl().getWordWrap()).isFalse();
    }

    @Test
    public void messageLogProperlyWrapWords_whenToggledOnce() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final MessageLogView view = new MessageLogView(executionService);
        view.postConstruct(shellProvider.getShell());
        
        view.toggleWordsWrapping();

        assertThat(view.getTextControl().getWordWrap()).isTrue();
    }

    @Test
    public void messageLogDoesNotWrapWords_whenToggledTwice() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final MessageLogView view = new MessageLogView(executionService);
        view.postConstruct(shellProvider.getShell());

        view.toggleWordsWrapping();
        view.toggleWordsWrapping();

        assertThat(view.getTextControl().getWordWrap()).isFalse();
    }
}
