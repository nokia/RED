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
import org.rf.ide.core.execution.Status;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestExecutionListener;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionStatusStore.ExecutionProgressListener;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionStatusStore.ExecutionTreeElementListener;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.ExecutionViewPropertyTester;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.GoToFileHandler.E4GoToFileHandler;
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
    
    @Inject
    private IEvaluationService evaluationService;

    public static final String ID = "org.robotframework.ide.ExecutionView";
    private static final String MENU_ID = "org.robotframework.ide.ExecutionView.viewer";

    private final RobotTestExecutionService executionService;
    private RobotTestsLaunch launch;
    private RobotTestExecutionListener executionListener;
    private final ExecutionTreeElementListener storeListener = this::refreshChangedNode;
    private final ExecutionProgressListener progressListener = this::refreshProgress;

    private Composite parent;

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
        this.parent = parent;
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
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(testsCounterLabel);
        testsCounterLabel.setText("Tests: 0/0");

        passCounterLabel = new CLabel(labelsComposite, SWT.NONE);
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(passCounterLabel);
        passCounterLabel.setImage(ImagesManager.getImage(RedImages.getSuccessImage()));
        passCounterLabel.setText("Passed: 0");

        failCounterLabel = new CLabel(labelsComposite, SWT.NONE);
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(failCounterLabel);
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

            final Optional<RobotTestsLaunch> lastLaunch = executionService.getLastLaunch();
            if (lastLaunch.isPresent()) {
                launch = lastLaunch.get();

                // this launch may be currently running, so we have to synchronize in order
                // to get proper state of messages, as other threads may change it in the meantime
                synchronized (launch) {
                    final ExecutionStatusStore elementsStore = launch.getExecutionData(ExecutionStatusStore.class,
                            ExecutionStatusStore::new);
                    elementsStore.addTreeListener(storeListener);
                    elementsStore.addProgressListener(progressListener);

                    SwtThread.asyncExec(() -> {
                        final ExecutionTreeNode root = elementsStore.getExecutionTree();
                        executionViewer.setInput(root == null ? null : newArrayList(root));
                        refreshProgress(elementsStore.getCurrentTest(), elementsStore.getPassedTests(),
                                elementsStore.getFailedTests(), elementsStore.getTotalTests());
                    });
                }
            }
        }
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
        provider.resetFailedFilter();

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
            executionService.forEachLaunch(launch -> launch.getExecutionData(ExecutionStatusStore.class)
                    .ifPresent(store -> store.removeStoreListener(storeListener, progressListener)));
        }
    }

    private void refreshChangedNode(final ExecutionStatusStore store, final ExecutionTreeNode node) {
        SwtThread.asyncExec(() -> {
            // it could have been queued earlier in main thread...
            if (parent == null || parent.isDisposed()) {
                return;
            }

            if (executionViewer.getInput() == null) {
                executionViewer.setInput(newArrayList(store.getExecutionTree()));
            }
            executionViewer.refresh(node);

            final List<ExecutionTreeNode> p = new ArrayList<>();
            ExecutionTreeNode e = node;
            while (e != null) {
                p.add(0, e);
                e = e.getParent();
            }

            final TreePath path = new TreePath(p.toArray());
            final Status status = node.getStatus().orElse(null);
            if (status == Status.RUNNING || status == Status.FAIL) {
                executionViewer.expandToLevel(path, 0);
            } else {
                executionViewer.collapseToLevel(path, p.size());
            }
        });
    }

    private void refreshProgress(final int currentTest, final int passedSoFar, final int failedSoFar,
            final int totalTests) {
        SwtThread.asyncExec(() -> {
            // it could have been queued earlier in main thread...
            if (parent == null || parent.isDisposed()) {
                return;
            }

            testsCounterLabel.setText(String.format("Tests: %d/%d", currentTest, totalTests));
            passCounterLabel.setText("Passed: " + passedSoFar);
            failCounterLabel.setText("Failed: " + failedSoFar);

            final Color progressBarColor = failedSoFar > 0 ? ColorsManager.getColor(180, 0, 0)
                    : ColorsManager.getColor(0, 180, 0);
            progressBar.setBarColor(progressBarColor);
            progressBar.setProgress(passedSoFar + failedSoFar, totalTests);
        });
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

            SwtThread.syncExec(() -> {
                evaluationService.requestEvaluation(ExecutionViewPropertyTester.PROPERTY_CURRENT_LAUNCH_IS_TERMINATED);
                actionBars.updateActionBars();
                clearView();
            });

            synchronized (ExecutionView.this.launch) {
                final ExecutionStatusStore elementsStore = launch.getExecutionData(ExecutionStatusStore.class,
                        ExecutionStatusStore::new);
                elementsStore.addTreeListener(storeListener);
                elementsStore.addProgressListener(progressListener);

                SwtThread.asyncExec(() -> {
                    refreshProgress(elementsStore.getCurrentTest(), elementsStore.getPassedTests(),
                            elementsStore.getFailedTests(), elementsStore.getTotalTests());
                });
            }
        }

        @Override
        public void executionEnded(final RobotTestsLaunch launch) {
            // nothing to do
            SwtThread.syncExec(() -> {
                evaluationService.requestEvaluation(ExecutionViewPropertyTester.PROPERTY_CURRENT_LAUNCH_IS_TERMINATED);
                actionBars.updateActionBars();
            });
        }
    }
}
