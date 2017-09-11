/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IEvaluationService;
import org.rf.ide.core.execution.agent.Status;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestExecutionListener;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.ExecutionViewPropertyTester;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.GoToFileHandler.E4GoToFileHandler;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.ShowFailedOnlyHandler;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.swt.SimpleProgressBar;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.Selections;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author mmarzec
 *
 */
public class ExecutionView {
    
    public static final String ID = "org.robotframework.ide.ExecutionView";
    private static final String MENU_ID = "org.robotframework.ide.ExecutionView.viewer";

    @Inject
    private IEvaluationService evaluationService;

    private ScheduledExecutorService executor;

    private final RobotTestExecutionService executionService;
    private RobotTestsLaunch launch;
    private RobotTestExecutionListener executionListener;

    private CLabel testsCounterLabel;
    private CLabel passCounterLabel;
    private CLabel failCounterLabel;
    
    private SimpleProgressBar progressBar;

    private TreeViewer executionViewer;
    
    private StyledText messageText;

    private IActionBars actionBars;

    public ExecutionView() {
        this(RedPlugin.getTestExecutionService());
    }

    @VisibleForTesting
    ExecutionView(final RobotTestExecutionService executionService) {
        this.executionService = executionService;
    }

    public TreeViewer getViewer() {
        return executionViewer;
    }

    public Optional<RobotTestsLaunch> getCurrentlyShownLaunch() {
        return Optional.ofNullable(launch);
    }

    @PostConstruct
    public void postConstruct(final Composite parent, final IViewPart part, final IMenuService menuService) {
        GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
        GridLayoutFactory.fillDefaults().applyTo(parent);

        createProgressLabels(parent);
        createProgressBar(parent);
        createExecutionTreeViewer(parent);
        createFailureMessageText(parent);

        createContextMenu(menuService);
        part.getViewSite().setSelectionProvider(executionViewer);
        actionBars = part.getViewSite().getActionBars();

        setInput();
    }

    private void createProgressLabels(final Composite parent) {
        final Composite labelsComposite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(3).applyTo(labelsComposite);
        GridDataFactory.fillDefaults().grab(true, false).indent(2, 8).applyTo(labelsComposite);

        testsCounterLabel = new CLabel(labelsComposite, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).hint(100, SWT.DEFAULT).applyTo(testsCounterLabel);
        testsCounterLabel.setText("Tests: 0/0");

        passCounterLabel = new CLabel(labelsComposite, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).hint(100, SWT.DEFAULT).applyTo(passCounterLabel);
        passCounterLabel.setImage(ImagesManager.getImage(RedImages.getSuccessImage()));
        passCounterLabel.setText("Passed: 0");

        failCounterLabel = new CLabel(labelsComposite, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).hint(100, SWT.DEFAULT).applyTo(failCounterLabel);
        failCounterLabel.setImage(ImagesManager.getImage(RedImages.getErrorImage()));
        failCounterLabel.setText("Failed: 0");
    }

    private void createProgressBar(final Composite parent) {
        progressBar = new SimpleProgressBar(parent);
        GridDataFactory.fillDefaults().grab(true, false).span(5, 1).hint(SWT.DEFAULT, 15).applyTo(progressBar);
    }

    private void createExecutionTreeViewer(final Composite parent) {
        executionViewer = new TreeViewer(parent);
        executionViewer.getTree().setHeaderVisible(false);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(executionViewer.getTree());
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(executionViewer.getTree());
        executionViewer.setContentProvider(new ExecutionViewContentProvider());

        executionViewer.addSelectionChangedListener(createSelectionChangedListener());
        executionViewer.addDoubleClickListener(createDoubleClickListener());

        ViewerColumnsFactory.newColumn("")
                .withWidth(300)
                .shouldGrabAllTheSpaceLeft(true)
                .labelsProvidedBy(new ExecutionViewLabelProvider())
                .createFor(executionViewer);
    }

    private void createFailureMessageText(final Composite parent) {
        messageText = new StyledText(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        messageText.setFont(JFaceResources.getTextFont());
        GridDataFactory.fillDefaults().grab(true, false).indent(3, 0).hint(0, 50).applyTo(messageText);
        GridLayoutFactory.fillDefaults().applyTo(messageText);
        messageText.setEditable(false);
        messageText.setAlwaysShowScrollBars(false);
    }

    private void createContextMenu(final IMenuService menuService) {
        final MenuManager mgr = new MenuManager();
        menuService.populateContributionManager(mgr, "popup:" + MENU_ID);
        executionViewer.getTree().setMenu(mgr.createContextMenu(executionViewer.getTree()));
    }
    
    private void setInput() {
        // synchronize on service, so that any thread which would like to start another launch
        // will have to wait
        synchronized (executionService) {
            executionListener = new ExecutionListener();
            executionService.addExecutionListener(executionListener);

            executionService.getLastLaunch().ifPresent(l -> {
                this.launch = l;
                setInput(launch);
            });
        }
    }

    private void setInput(final RobotTestsLaunch launch) {
        if (executor != null) {
            executor.shutdownNow();
        }
        SwtThread.syncExec(this::resetView);

        // this launch may be currently running, so we have to synchronize in order
        // to get proper state of messages, as other threads may change it in the meantime
        synchronized (launch) {
            final ExecutionStatusStore elementsStore = launch.getExecutionData(ExecutionStatusStore.class,
                    ExecutionStatusStore::new);

            if (launch.isTerminated()) {
                refreshEverything(elementsStore);
            } else {
                executor = Executors.newScheduledThreadPool(1);
                final Runnable command = () -> {
                    if (elementsStore.checkDirtyAndReset()) {
                        SwtThread.asyncExec(() -> {
                            refreshEverything(elementsStore);
                        });
                    }
                };
                executor.scheduleAtFixedRate(command, 0, 300, TimeUnit.MILLISECONDS);
            }
        }
    }

    private void resetView() {
        executionViewer.setInput(null);
        messageText.setText("");

        testsCounterLabel.setText("Tests: 0/0");
        passCounterLabel.setText("Passed: 0");
        failCounterLabel.setText("Failed: 0");

        progressBar.reset();

        executionViewer.refresh();
    }

    private void refreshEverything(final ExecutionStatusStore elementsStore) {
        setProgress(elementsStore.getCurrentTest(), elementsStore.getPassedTests(), elementsStore.getFailedTests(),
                elementsStore.getTotalTests());

        executionViewer.getTree().setRedraw(false);
        try {
            final ExecutionTreeNode root = elementsStore.getExecutionTree();
            executionViewer.setInput(root == null ? null : newArrayList(root));
            if (root != null) {
                expandAllFailedOrRunning(root);
            }
        } finally {
            executionViewer.getTree().setRedraw(true);
        }
    }

    private void expandAllFailedOrRunning(final ExecutionTreeNode root) {
        final List<TreePath> elementsToExpand = new ArrayList<>();
        collectElementsToExpand(elementsToExpand, root);

        executionViewer.setExpandedTreePaths(elementsToExpand.toArray(new TreePath[0]));
    }

    private void collectElementsToExpand(final List<TreePath> elementsToExpand, final ExecutionTreeNode node) {
        final Status status = node.getStatus().orElse(null);
        if (status == Status.FAIL || status == Status.RUNNING) {
            elementsToExpand.add(new TreePath(getPath(node).toArray()));
            for (final ExecutionTreeNode child : node.getChildren()) {
                collectElementsToExpand(elementsToExpand, child);
            }
        }
        
    }

    private List<ExecutionTreeNode> getPath(final ExecutionTreeNode node) {
        final List<ExecutionTreeNode> path = new ArrayList<>();
        ExecutionTreeNode current = node;
        while (current != null) {
            path.add(0, current);
            current = current.getParent();
        }
        return path;
    }

    @Focus
    public void onFocus() {
        executionViewer.getControl().setFocus();
    }

    public void clearView() {
        executionViewer.setInput(null);
        messageText.setText("");

        final ExecutionViewContentProvider provider = (ExecutionViewContentProvider) executionViewer
                .getContentProvider();
        provider.setFailedFilter(false);
        ShowFailedOnlyHandler.setCommandState(false);

        testsCounterLabel.setText("Tests: 0/0");
        passCounterLabel.setText("Passed: 0");
        failCounterLabel.setText("Failed: 0");

        progressBar.reset();

        executionViewer.refresh();
        
        evaluationService.requestEvaluation(ExecutionViewPropertyTester.PROPERTY_CURRENT_LAUNCH_EXEC_STORE_IS_DISPOSED);
    }

    @PreDestroy
    public void dispose() {
        synchronized (executionService) {
            executionService.removeExecutionListener(executionListener);
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void setProgress(final int currentTest, final int passedSoFar, final int failedSoFar,
            final int totalTests) {
        testsCounterLabel.setText(String.format("Tests: %d/%d", currentTest, totalTests));
        passCounterLabel.setText("Passed: " + passedSoFar);
        failCounterLabel.setText("Failed: " + failedSoFar);

        final Color progressBarColor = failedSoFar > 0 ? ColorsManager.getColor(180, 0, 0)
                : ColorsManager.getColor(0, 180, 0);
        progressBar.setBarColor(progressBarColor);
        progressBar.setProgress(passedSoFar + failedSoFar, totalTests);
    }

    private ISelectionChangedListener createSelectionChangedListener() {
        return new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                final String message = Selections.getOptionalFirstElement(selection, ExecutionTreeNode.class)
                        .map(ExecutionTreeNode::getMessage)
                        .orElse("");
                messageText.setText(message);
            }
        };
    }

    private IDoubleClickListener createDoubleClickListener() {
        return event -> Selections
                .getOptionalFirstElement((IStructuredSelection) event.getSelection(), ExecutionTreeNode.class)
                .ifPresent(node -> E4GoToFileHandler.openExecutionNodeSourceFile(node));
    }

    private class ExecutionListener implements RobotTestExecutionListener {

        @Override
        public void executionStarting(final RobotTestsLaunch launch) {
            ExecutionView.this.launch = launch;

            SwtThread.asyncExec(() -> {
                evaluationService.requestEvaluation(ExecutionViewPropertyTester.PROPERTY_CURRENT_LAUNCH_IS_TERMINATED);
                actionBars.updateActionBars();

                final ExecutionViewContentProvider provider = (ExecutionViewContentProvider) executionViewer
                        .getContentProvider();
                provider.setFailedFilter(false);
                ShowFailedOnlyHandler.setCommandState(false);
            });

            setInput(launch);
        }

        @Override
        public void executionEnded(final RobotTestsLaunch launch) {
            SwtThread.asyncExec(() -> {
                evaluationService.requestEvaluation(ExecutionViewPropertyTester.PROPERTY_CURRENT_LAUNCH_IS_TERMINATED);
                actionBars.updateActionBars();
            });
            // execution ended, however the statys store can still be updated by server thread;
            // we're scheduling a single task (last one as executor will be shutdown) which will
            // wait for store to be closed and then will set the input for the view
            executor.schedule(() -> {
                final ExecutionStatusStore messagesStore = launch.getExecutionData(ExecutionStatusStore.class,
                        ExecutionStatusStore::new);
                while (messagesStore.isOpen()) {
                    try {
                        Thread.sleep(200);
                    } catch (final InterruptedException e) {
                        // fine, let's wait more
                    }
                }
                SwtThread.asyncExec(() -> {
                    setInput(launch);
                });

            }, 0, TimeUnit.SECONDS);
            executor.shutdown();
        }
    }
}
