/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.rf.ide.core.execution.ExecutionElement;
import org.rf.ide.core.execution.ExecutionElement.ExecutionElementType;
import org.rf.ide.core.execution.Status;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestExecutionListener;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.DefinitionPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.TestCasesDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.TestCasesDefinitionLocator.TestCaseDetector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor.RobotEditorOpeningException;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionElementsStore.ExecutionElementsStoreListener;
import org.robotframework.red.actions.CollapseAllAction;
import org.robotframework.red.actions.ExpandAllAction;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.Selections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

/**
 * @author mmarzec
 *
 */
@SuppressWarnings({ "PMD.GodClass", "PMD.TooManyMethods" })
public class ExecutionView {
    
    public static final String ID = "org.robotframework.ide.ExecutionView";

    @Inject
    protected IEventBroker eventBroker;
    
    private final RobotTestExecutionService executionService;

    private Label passCounterLabel;

    private Label failCounterLabel;
    
    private int passCounter = 0;

    private int failCounter = 0;
    
    private TreeViewer executionViewer;
    
    private ExecutionViewContentProvider executionViewContentProvider;
    
    private StyledText messageText;
    
    private ShowFailedOnlyAction showFailedAction;
    
    private RerunFailedOnlyAction rerunFailedOnlyAction;

    private final List<ExecutionStatus> executionViewerInput = new ArrayList<>();

    private final LinkedList<ExecutionStatus> suitesStack = new LinkedList<>();

    private RobotTestExecutionListener executionListener;
    
    private final ExecutionElementsStoreListener storeListener =
            (store, elem) -> SwtThread.syncExec(() -> executionEvent(elem));

    public ExecutionView() {
        this(RedPlugin.getTestExecutionService());
    }

    @VisibleForTesting
    ExecutionView(final RobotTestExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostConstruct
    public void postConstruct(final Composite parent, final IViewPart part) {
        GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parent);

        final Composite labelsComposite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(4).applyTo(labelsComposite);
        GridDataFactory.fillDefaults().grab(true, false).indent(2, 2).applyTo(labelsComposite);

        final Label passImageLabel = new Label(labelsComposite, SWT.NONE);
        passImageLabel.setImage(ImagesManager.getImage(RedImages.getSuccessImage()));
        passCounterLabel = new Label(labelsComposite, SWT.NONE);
        GridDataFactory.fillDefaults().hint(70, 15).applyTo(passCounterLabel);
        passCounterLabel.setText("Passed: 0");

        final Label failImageLabel = new Label(labelsComposite, SWT.NONE);
        failImageLabel.setImage(ImagesManager.getImage(RedImages.getErrorImage()));
        failCounterLabel = new Label(labelsComposite, SWT.NONE);
        GridDataFactory.fillDefaults().hint(70, 15).applyTo(failCounterLabel);
        failCounterLabel.setText("Failed: 0");

        executionViewer = new TreeViewer(parent);
        executionViewer.getTree().setHeaderVisible(false);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(executionViewer.getTree());
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(executionViewer.getTree());
        executionViewContentProvider = new ExecutionViewContentProvider();
        executionViewer.setContentProvider(executionViewContentProvider);

        executionViewer.addSelectionChangedListener(createSelectionChangedListener());
        executionViewer.addDoubleClickListener(createDoubleClickListener());
        final Menu menu = createContextMenu();
        executionViewer.getTree().addMouseListener(createMouseListener(menu));
        
        ViewerColumnsFactory.newColumn("")
                .withWidth(300)
                .shouldGrabAllTheSpaceLeft(true)
                .labelsProvidedBy(new ExecutionViewLabelProvider())
                .createFor(executionViewer);

        setViewerInput();

        messageText = new StyledText(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        messageText.setFont(JFaceResources.getTextFont());
        GridDataFactory.fillDefaults().grab(true, false).indent(3, 0).hint(0, 50).applyTo(messageText);
        GridLayoutFactory.fillDefaults().applyTo(messageText);
        messageText.setEditable(false);
        messageText.setAlwaysShowScrollBars(false);
        
        createToolbarActions(part.getViewSite().getActionBars().getToolBarManager());

        setInput();
    }
    
    private void setInput() {
        // synchronize on service, so that any thread which would like to start another launch
        // will have to wait
        synchronized (executionService) {
            executionListener = new ExecutionListener(storeListener);
            executionService.addExecutionListener(executionListener);

            final Optional<RobotTestsLaunch> lastLaunch = executionService.getLastLaunch();
            if (lastLaunch.isPresent()) {
                final RobotTestsLaunch launch = lastLaunch.get();

                // this launch may be currently running, so we have to synchronize in order
                // to get proper state of messages, as other threads may change it in the meantime
                synchronized (launch) {
                    final ExecutionElementsStore elementsStore = launch.getExecutionData(ExecutionElementsStore.class,
                            ExecutionElementsStore::new);
                    elementsStore.addStoreListener(storeListener);

                    final List<ExecutionElement> currentElements = elementsStore.getElements();
                    SwtThread.syncExec(() -> executionEvents(currentElements));
                }
            }
        }
    }

    @Focus
    public void onFocus() {
        executionViewer.getControl().setFocus();
    }

    @PreDestroy
    public void dispose() {
        synchronized (executionService) {
            executionService.removeExecutionListener(executionListener);
            executionService.forEachLaunch(launch -> launch.getExecutionData(ExecutionElementsStore.class)
                    .ifPresent(store -> store.removeStoreListener(storeListener)));
        }
    }

    private void executionEvents(final List<ExecutionElement> executionElements) {
        for (final ExecutionElement executionElement : executionElements) {
            executionEvent(executionElement);
        }
    }

    private void executionEvent(final ExecutionElement executionElement) {
        if (isSuiteStartEvent(executionElement)) {
            handleSuiteStartEvent(executionElement);
        } else if (isTestStartEvent(executionElement)) {
            handleTestStartEvent(executionElement);
        } else if (isSuiteEndEvent(executionElement)) {
            handleSuiteEndEvent(executionElement);
        } else if (isTestEndEvent(executionElement)) {
            handleTestEndEvent(executionElement);
        } else if (isOutputFileEvent(executionElement)) {
            handleOutputFileEvent(executionElement);
        }
        refreshView();
    }

    private void clearView() {
        suitesStack.clear();
        executionViewerInput.clear();
        setViewerInput();
        passCounter = 0;
        failCounter = 0;
        messageText.setText("");
        executionViewContentProvider.setFailedFilterEnabled(false);
        showFailedAction.setChecked(false);
        rerunFailedOnlyAction.setOutputFilePath(null);
        refreshView();
    }

    private void refreshView() {
        passCounterLabel.setText("Passed: " + passCounter);
        failCounterLabel.setText("Failed: " + failCounter);
        executionViewer.refresh();
    }
    
    private ISelectionChangedListener createSelectionChangedListener() {
        return new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final String message = Selections
                        .getOptionalFirstElement((IStructuredSelection) event.getSelection(), ExecutionStatus.class)
                        .map(status -> Strings.nullToEmpty(status.getMessage()))
                        .orElse("");
                messageText.setText(message);
            }
        };
    }

    private IDoubleClickListener createDoubleClickListener() {
        return event -> Selections
                .getOptionalFirstElement((IStructuredSelection) event.getSelection(), ExecutionStatus.class)
                        .ifPresent(status -> openExecutionStatusSourceFile(status));
    }
    
    private Menu createContextMenu() {
        final Menu menu = new Menu(executionViewer.getTree());
        final MenuItem gotoItem = new MenuItem(menu, SWT.PUSH);
        gotoItem.setText("Go to File");
        gotoItem.setImage(ImagesManager.getImage(RedImages.getGoToImage()));
        gotoItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent event) {
                final IStructuredSelection selection = (IStructuredSelection) executionViewer.getSelection();
                Selections.getOptionalFirstElement(selection, ExecutionStatus.class)
                        .ifPresent(status -> openExecutionStatusSourceFile(status));
            }
        });
        return menu;
    }
    
    private MouseAdapter createMouseListener(final Menu menu) {
        return new MouseAdapter() {

            @Override
            public void mouseDown(final MouseEvent e) {
                if (e.button == 3 && executionViewer.getTree().getSelectionCount() == 1) {
                    final TreeSelection selection = (TreeSelection) executionViewer.getSelection();
                    if (selection != null
                            && ((ExecutionStatus) selection.getFirstElement()).getType() == ExecutionElementType.TEST) {
                        menu.setVisible(true);
                    }
                }
            }
        };
    }
    
    private void openExecutionStatusSourceFile(final ExecutionStatus executionStatus) {
        if (executionStatus == null || executionStatus.getSource() == null) {
            return;
        }
        final IPath sourcePath = new Path(executionStatus.getSource());
        final IFile sourceFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(sourcePath);
        if (sourceFile == null || !sourceFile.exists()) {
            return;
        }
        new TestCasesDefinitionLocator(sourceFile)
                .locateTestCaseDefinition(createDetector(sourceFile, executionStatus.getName()));
    }

    private TestCaseDetector createDetector(final IFile sourceFile, final String caseName) {
        return new TestCaseDetector() {

            @Override
            public ContinueDecision testCaseDetected(final RobotSuiteFile file, final RobotCase testCase) {
                if (testCase.getName().equals(caseName)) {
                    final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
                    final IEditorDescriptor desc = editorRegistry.findEditor(RobotFormEditor.ID);
                    try {
                        final IEditorPart editor = PlatformUI.getWorkbench()
                                .getActiveWorkbenchWindow()
                                .getActivePage()
                                .openEditor(new FileEditorInput(sourceFile), desc.getId());
                        if (editor instanceof RobotFormEditor) {
                            final SuiteSourceEditor sourcePage = ((RobotFormEditor) editor).activateSourcePage();
                            final DefinitionPosition position = testCase.getDefinitionPosition();
                            sourcePage.getSelectionProvider()
                                    .setSelection(new TextSelection(position.getOffset(), position.getLength()));

                        }
                    } catch (final PartInitException e) {
                        throw new RobotEditorOpeningException("Unable to open editor for file: " + sourceFile.getName(),
                                e);
                    }

                    return ContinueDecision.STOP;
                } else {
                    return ContinueDecision.CONTINUE;
                }
            }
        };
    }
    
    private void createToolbarActions(final IToolBarManager toolBarManager) {
        toolBarManager.add(new ExpandAllAction(executionViewer));
        toolBarManager.add(new CollapseAllAction(executionViewer));
        showFailedAction = new ShowFailedOnlyAction(executionViewer, executionViewContentProvider);
        toolBarManager.add(showFailedAction);
        toolBarManager.add(new RerunAction());
        rerunFailedOnlyAction = new RerunFailedOnlyAction();
        toolBarManager.add(rerunFailedOnlyAction);
    }
    
    private boolean isSuiteStartEvent(final ExecutionElement executionElement) {
        return executionElement.getStatus() == null && executionElement.getType() == ExecutionElementType.SUITE;
    }
    
    private boolean isTestStartEvent(final ExecutionElement executionElement) {
        return executionElement.getStatus() == null && executionElement.getType() == ExecutionElementType.TEST;
    }
    
    private boolean isSuiteEndEvent(final ExecutionElement executionElement) {
        return executionElement.getStatus() != null && executionElement.getType() == ExecutionElementType.SUITE;
    }
    
    private boolean isTestEndEvent(final ExecutionElement executionElement) {
        return executionElement.getStatus() != null && executionElement.getType() == ExecutionElementType.TEST;
    }
    
    private boolean isOutputFileEvent(final ExecutionElement executionElement) {
        return executionElement.getType() == ExecutionElementType.OUTPUT_FILE;
    }
    
    private void handleSuiteStartEvent(final ExecutionElement executionElement) {
        final ExecutionStatus newSuiteExecutionStatus = new ExecutionStatus(executionElement.getName(), Status.RUNNING,
                executionElement.getType(), new ArrayList<ExecutionStatus>());
        if (suitesStack.isEmpty()) {
            suitesStack.add(newSuiteExecutionStatus);
            executionViewerInput.add(newSuiteExecutionStatus);
            setViewerInput();
        } else {
            final ExecutionStatus lastSuite = suitesStack.getLast();
            newSuiteExecutionStatus.setParent(lastSuite);
            newSuiteExecutionStatus.setSource(executionElement.getSource().getAbsolutePath());
            lastSuite.addChildren(newSuiteExecutionStatus);
            suitesStack.addLast(newSuiteExecutionStatus);
        }
        executionViewer.expandToLevel(newSuiteExecutionStatus, 1);
    }
    
    private void handleTestStartEvent(final ExecutionElement executionElement) {
        final ExecutionStatus newTestExecutionStatus = new ExecutionStatus(executionElement.getName(), Status.RUNNING,
                executionElement.getType(), new ArrayList<ExecutionStatus>());
        if (!suitesStack.isEmpty()) {
            final ExecutionStatus lastSuite = suitesStack.getLast();
            newTestExecutionStatus.setParent(lastSuite);
            newTestExecutionStatus.setSource(lastSuite.getSource());
            lastSuite.addChildren(newTestExecutionStatus);
            executionViewer.reveal(newTestExecutionStatus);
        }
    }
    
    private void handleSuiteEndEvent(final ExecutionElement executionElement) {
        if (!suitesStack.isEmpty()) {
            final ExecutionStatus lastSuite = suitesStack.getLast();
            final int elapsedTime = executionElement.getElapsedTime();
            lastSuite.setElapsedTime(String.valueOf(((double) elapsedTime) / 1000));
            final Status status = executionElement.getStatus();
            lastSuite.setStatus(status);
            if (suitesStack.size() > 1) {
                suitesStack.removeLast();
                if (status == Status.PASS) {
                    executionViewer.collapseToLevel(lastSuite, AbstractTreeViewer.ALL_LEVELS);
                }
            }
        }
    }
    
    private void handleTestEndEvent(final ExecutionElement executionElement) {
        if (!suitesStack.isEmpty()) {
            final ExecutionStatus lastSuite = suitesStack.getLast();
            final List<ExecutionStatus> lastSuiteChildren = lastSuite.getChildren();
            final Status status = executionElement.getStatus();
            final int elapsedTime = executionElement.getElapsedTime();
            for (final ExecutionStatus executionStatus : lastSuiteChildren) {
                if (executionStatus.getName().equals(executionElement.getName()) && executionStatus.getStatus() == Status.RUNNING) {
                    executionStatus.setStatus(status);
                    final String message = executionElement.getMessage();
                    if (message != null && !message.equals("")) {
                        executionStatus.setMessage(message);
                    }
                    executionStatus.setElapsedTime(String.valueOf(((double) elapsedTime) / 1000));
                    executionViewer.reveal(executionStatus);
                    break;
                }
            }

            if (status == Status.PASS) {
                passCounter++;
            } else {
                failCounter++;
            }
        }
    }
    
    private void handleOutputFileEvent(final ExecutionElement executionElement) {
        rerunFailedOnlyAction.setOutputFilePath(executionElement.getName());
    }
    
    private void setViewerInput() {
        executionViewer.setInput(executionViewerInput.toArray(new ExecutionStatus[executionViewerInput.size()]));
    }

    private class ExecutionListener implements RobotTestExecutionListener {

        private final ExecutionElementsStoreListener storeListener;

        ExecutionListener(final ExecutionElementsStoreListener storeListener) {
            this.storeListener = storeListener;
        }

        @Override
        public void executionStarting(final RobotTestsLaunch launch) {
            SwtThread.syncExec(() -> clearView());

            launch.getExecutionData(ExecutionElementsStore.class, ExecutionElementsStore::new)
                    .addStoreListener(storeListener);
        }

        @Override
        public void executionEnded(final RobotTestsLaunch launch) {
            launch.performOnExecutionData(ExecutionElementsStore.class,
                    store -> store.removeStoreListener(storeListener));
        }
    }
}
