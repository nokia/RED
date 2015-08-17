package org.robotframework.ide.eclipse.main.plugin.views;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.core.execution.ExecutionElement;
import org.robotframework.ide.core.execution.ExecutionElement.ExecutionElementType;
import org.robotframework.ide.core.execution.ExecutionElementsParser;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.execution.CollapseAllAction;
import org.robotframework.ide.eclipse.main.plugin.execution.ExecutionStatus;
import org.robotframework.ide.eclipse.main.plugin.execution.ExecutionStatus.Status;
import org.robotframework.ide.eclipse.main.plugin.execution.ExecutionViewContentProvider;
import org.robotframework.ide.eclipse.main.plugin.execution.ExecutionViewLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.execution.ExpandAllAction;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author mmarzec
 *
 */
public class ExecutionView {
    
    public static final String ID = "org.robotframework.ide.ExecutionView";

    private CLabel passCounterLabel;

    private CLabel failCounterLabel;
    
    private int passCounter = 0;

    private int failCounter = 0;
    
    private TreeViewer executionViewer;
    
    private StyledText messageText;

    private List<ExecutionStatus> executionViewerInput = new ArrayList<>();

    private LinkedList<ExecutionStatus> suitesStack = new LinkedList<>();

    @PostConstruct
    public void postConstruct(final Composite parent, final IViewPart part) {

        GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parent);

        final Composite labelsComposite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(labelsComposite);
        GridDataFactory.fillDefaults().applyTo(labelsComposite);

        passCounterLabel = new CLabel(labelsComposite, SWT.NONE);
        passCounterLabel.setImage(ImagesManager.getImage(RedImages.getSuccessImage()));
        passCounterLabel.setText("Passed: 0");

        failCounterLabel = new CLabel(labelsComposite, SWT.NONE);
        failCounterLabel.setImage(ImagesManager.getImage(RedImages.getErrorImage()));
        failCounterLabel.setText("Failed: 0");

        executionViewer = new TreeViewer(parent);
        executionViewer.getTree().setHeaderVisible(false);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(executionViewer.getTree());
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(executionViewer.getTree());
        ViewerColumnsFactory.newColumn("Status")
                .labelsProvidedBy(new ExecutionViewLabelProvider())
                .withMinWidth(400)
                .createFor(executionViewer);
        executionViewer.setContentProvider(new ExecutionViewContentProvider());
        executionViewer.setInput(executionViewerInput.toArray(new ExecutionStatus[executionViewerInput.size()]));
        executionViewer.addSelectionChangedListener(createSelectionChangedListener());
        executionViewer.addDoubleClickListener(createDoubleClickListener());

        messageText = new StyledText(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        messageText.setFont(JFaceResources.getTextFont());
        GridDataFactory.fillDefaults().grab(true, false).indent(3, 0).hint(0, 50).applyTo(messageText);
        GridLayoutFactory.fillDefaults().applyTo(messageText);
        messageText.setEditable(false);
        messageText.setAlwaysShowScrollBars(false);
        
        createToolbarActions(part.getViewSite().getActionBars().getToolBarManager());
    }
    
    @Focus
    public void onFocus() {
        executionViewer.getControl().setFocus();
    }

    @PreDestroy
    public void dispose() {
        
    }
    
    @Inject
    @Optional
    private void executionEvent(@UIEventTopic("ExecutionView/ExecutionEvent") final ExecutionElement executionElement) {
        
        if (isSuiteStartEvent(executionElement)) {
            handleSuiteStartEvent(executionElement);
        } else if (isTestStartEvent(executionElement)) {
            handleTestStartEvent(executionElement);
        } else if (isSuiteEndEvent(executionElement)) {
            handleSuiteEndEvent(executionElement);
        } else if (isTestEndEvent(executionElement)) {
            handleTestEndEvent(executionElement);
        }

        refreshViewer();
    }

    @Inject
    @Optional
    private void clearEvent(@UIEventTopic("ExecutionView/ClearEvent") final String s) {
        suitesStack.clear();
        executionViewerInput.clear();
        passCounter = 0;
        failCounter = 0;
        executionViewer.refresh();
    }

    private void refreshViewer() {
        passCounterLabel.setText("Passed: " + passCounter);
        failCounterLabel.setText("Failed: " + failCounter);
        executionViewer.refresh();
    }
    
    private ISelectionChangedListener createSelectionChangedListener() {
        return new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final ExecutionStatus status = (ExecutionStatus) ((TreeSelection) event.getSelection()).getFirstElement();
                if (status != null) {
                    final String message = status.getMessage();
                    if (message != null && !message.equals("")) {
                        messageText.setText(message);
                    } else {
                        messageText.setText("");
                    }
                }
            }
        };
    }

    private IDoubleClickListener createDoubleClickListener() {
        return new IDoubleClickListener() {

            @Override
            public void doubleClick(final DoubleClickEvent event) {
                if (event.getSelection() != null && event.getSelection() instanceof StructuredSelection) {
                    final StructuredSelection selection = (StructuredSelection) event.getSelection();
                    if (!selection.isEmpty()) {
                        final ExecutionStatus executionStatus = (ExecutionStatus) selection.getFirstElement();
                        if (executionStatus != null && executionStatus.getSource() != null) {
                            final IPath sourcePath = new Path(executionStatus.getSource());
                            final IFile sourceFile = ResourcesPlugin.getWorkspace()
                                    .getRoot()
                                    .getFileForLocation(sourcePath);
                            if (sourceFile != null && sourceFile.exists()) {
                                final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
                                final IEditorDescriptor desc = editorRegistry.findEditor(RobotFormEditor.ID);
                                try {
                                    final IEditorPart editor = PlatformUI.getWorkbench()
                                            .getActiveWorkbenchWindow()
                                            .getActivePage()
                                            .openEditor(new FileEditorInput(sourceFile), desc.getId());
                                    if (editor instanceof RobotFormEditor) {
                                        ((RobotFormEditor) editor).activateSourcePage();
                                    }
                                } catch (final PartInitException e) {
                                    throw new RuntimeException("Unable to open editor for file: "
                                            + sourceFile.getName(), e);
                                }
                            }
                        }
                    }
                }
            }
        };
    }
    
    private void createToolbarActions(final IToolBarManager toolBarManager) {
        final ExpandAllAction expandAction = new ExpandAllAction(executionViewer);
        expandAction.setText("Expand All");
        expandAction.setImageDescriptor(RedImages.getExpandAllImage());
        toolBarManager.add(expandAction);
        final CollapseAllAction collapseAction = new CollapseAllAction(executionViewer);
        collapseAction.setText("Collapse All");
        collapseAction.setImageDescriptor(RedImages.getCollapseAllImage());
        toolBarManager.add(collapseAction);
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
    
    private void handleSuiteStartEvent(final ExecutionElement executionElement) {
        final ExecutionStatus newSuiteExecutionStatus = new ExecutionStatus(executionElement.getName(), Status.RUNNING,
                executionElement.getType(), new ArrayList<ExecutionStatus>());
        if (suitesStack.isEmpty()) {
            suitesStack.add(newSuiteExecutionStatus);
            executionViewerInput.add(newSuiteExecutionStatus);
            executionViewer.setInput(executionViewerInput.toArray(new ExecutionStatus[executionViewerInput.size()]));
        } else {
            final ExecutionStatus lastSuite = suitesStack.getLast();
            newSuiteExecutionStatus.setParent(lastSuite);
            newSuiteExecutionStatus.setSource(executionElement.getSource());
            lastSuite.addChildren(newSuiteExecutionStatus);
            suitesStack.addLast(newSuiteExecutionStatus);
        }
        executionViewer.expandToLevel(newSuiteExecutionStatus, 1);
    }
    
    private void handleTestStartEvent(final ExecutionElement executionElement) {
        final ExecutionStatus newTestExecutionStatus = new ExecutionStatus(executionElement.getName(), Status.RUNNING,
                executionElement.getType(), new ArrayList<ExecutionStatus>());
        final ExecutionStatus lastSuite = suitesStack.getLast();
        newTestExecutionStatus.setParent(lastSuite);
        newTestExecutionStatus.setSource(lastSuite.getSource());
        lastSuite.addChildren(newTestExecutionStatus);
        executionViewer.expandToLevel(newTestExecutionStatus, 1);
    }
    
    private void handleSuiteEndEvent(final ExecutionElement executionElement) {
        final ExecutionStatus lastSuite = suitesStack.getLast();
        final int elapsedTime = executionElement.getElapsedTime();
        lastSuite.setElapsedTime(String.valueOf(((double) elapsedTime) / 1000));
        final Status status = getStatus(executionElement);
        lastSuite.setStatus(status);
        if (suitesStack.size() > 1) {
            suitesStack.removeLast();
            if (status == Status.PASS) {
                executionViewer.collapseToLevel(lastSuite, TreeViewer.ALL_LEVELS);
            }
        }
    }
    
    private void handleTestEndEvent(final ExecutionElement executionElement) {
        final ExecutionStatus lastSuite = suitesStack.getLast();
        final List<ExecutionStatus> lastSuiteChildren = lastSuite.getChildren();
        final Status status = getStatus(executionElement);
        final int elapsedTime = executionElement.getElapsedTime();
        for (ExecutionStatus executionStatus : lastSuiteChildren) {
            if (executionStatus.getName().equals(executionElement.getName())) {
                executionStatus.setStatus(status);
                final String message = executionElement.getMessage();
                if (message != null && !message.equals("")) {
                    executionStatus.setMessage(message);
                }
                executionStatus.setElapsedTime(String.valueOf(((double) elapsedTime) / 1000));
                break;
            }
        }

        if (status == Status.PASS) {
            passCounter++;
        } else {
            failCounter++;
        }
    }
    
    private Status getStatus(final ExecutionElement executionElement) {
        return executionElement.getStatus().equals(ExecutionElementsParser.ROBOT_EXECUTION_PASS_STATUS) ? Status.PASS
                : Status.FAIL;
    }
}
