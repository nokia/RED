/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.function.Predicate;

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.menus.IMenuService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.execution.agent.Status;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.swt.SimpleProgressBar;

public class ExecutionViewTest {

    @Rule
    public final ShellProvider shellProvider = new ShellProvider();

    private final IMenuService menuService = mock(IMenuService.class);
    private final IViewPart part = mock(IViewPart.class);
    private final IViewSite viewSite = mock(IViewSite.class);

    @Before
    public void beforeTest() {
        when(part.getViewSite()).thenReturn(viewSite);
    }

    @Test
    public void executionViewHasEmptyViewerAndNoProgress_whenThereWereNoLaunchesSoFar() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final Shell shell = shellProvider.getShell();
        final ExecutionView view = new ExecutionView(executionService);
        view.postConstruct(shell, part, menuService);

        assertThat(view.getViewer().getInput()).isNull();
        assertThat(getTotalTestsLabel(shell).getText()).isEqualTo("Tests: 0/0");
        assertThat(getPassedTestsLabel(shell).getText()).isEqualTo("Passed: 0");
        assertThat(getFailedTestsLabel(shell).getText()).isEqualTo("Failed: 0");
        assertThat(getErrorMessageText(shell).getText()).isEmpty();
        assertThat(getProgressBar(shell).getProgress()).isCloseTo(0.0, withinPercentage(0.001));
    }

    @Test
    public void executionViewHasTreeAndProgressFromLastLaunchAfterConstruction_whenThereWereLaunches()
            throws Exception {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final RobotTestsLaunch launch1 = executionService.testExecutionStarting(null);
        final ExecutionStatusStore store1 = new ExecutionStatusStore();
        store1.open();
        launch1.getExecutionData(ExecutionStatusStore.class, () -> store1);
        store1.suiteStarted("suite", new URI("file:///suite"), 2, new ArrayList<>(), newArrayList("t1", "t2"));
        store1.testStarted();
        store1.elementEnded(1000, Status.PASS, "");
        store1.testStarted();
        store1.elementEnded(2000, Status.FAIL, "error");
        store1.elementEnded(3000, Status.FAIL, "");
        executionService.testExecutionEnded(launch1);

        final RobotTestsLaunch launch2 = executionService.testExecutionStarting(null);
        final ExecutionStatusStore store2 = new ExecutionStatusStore();
        store2.open();
        launch2.getExecutionData(ExecutionStatusStore.class, () -> store2);
        store2.suiteStarted("suite2", new URI("file:///suite2"), 2, new ArrayList<>(), newArrayList("tx", "ty"));
        store2.testStarted();
        store2.elementEnded(1000, Status.PASS, "");
        store2.testStarted();
        store2.elementEnded(2000, Status.PASS, "");
        store2.elementEnded(3000, Status.PASS, "");
        executionService.testExecutionEnded(launch2);

        final Shell shell = shellProvider.getShell();
        final ExecutionView view = new ExecutionView(executionService);
        view.postConstruct(shell, part, menuService);

        execAllAwaitingMessages();

        assertThat(view.getViewer().getInput()).isNotNull();
        assertThat(getTotalTestsLabel(shell).getText()).isEqualTo("Tests: 2/2");
        assertThat(getPassedTestsLabel(shell).getText()).isEqualTo("Passed: 2");
        assertThat(getFailedTestsLabel(shell).getText()).isEqualTo("Failed: 0");
        assertThat(getErrorMessageText(shell).getText()).isEmpty();
        assertThat(getProgressBar(shell).getProgress()).isCloseTo(1.0, withinPercentage(0.001));
    }

    private void execAllAwaitingMessages() {
        while (Display.getDefault().readAndDispatch()) {
            // nothing to do
        }
    }

    private static CLabel getTotalTestsLabel(final Composite parent) {
        return (CLabel) findControlSatisfying(parent,
                ctrl -> ctrl instanceof CLabel && ((CLabel) ctrl).getText().startsWith("Tests"));
    }

    private static CLabel getPassedTestsLabel(final Composite parent) {
        return (CLabel) findControlSatisfying(parent,
                ctrl -> ctrl instanceof CLabel && ((CLabel) ctrl).getText().startsWith("Passed"));
    }

    private static CLabel getFailedTestsLabel(final Composite parent) {
        return (CLabel) findControlSatisfying(parent,
                ctrl -> ctrl instanceof CLabel && ((CLabel) ctrl).getText().startsWith("Failed"));
    }

    private static SimpleProgressBar getProgressBar(final Composite parent) {
        return (SimpleProgressBar) findControlSatisfying(parent, ctrl -> ctrl instanceof SimpleProgressBar);
    }

    private static StyledText getErrorMessageText(final Composite parent) {
        return (StyledText) findControlSatisfying(parent, ctrl -> ctrl instanceof StyledText);
    }

    private static Control findControlSatisfying(final Composite parent, final Predicate<Control> predicate) {
        for (final Control control :  parent.getChildren()) {
            if (predicate.test(control)) {
                return control;
            }
            if (control instanceof Composite) {
                final Control c = findControlSatisfying((Composite) control, predicate);
                if (c != null) {
                    return c;
                }
            }
        }
        return null;
    }

}
