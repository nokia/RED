package org.robotframework.ide.eclipse.main.plugin.views;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.robotframework.ide.core.execution.ExecutionElement;
import org.robotframework.ide.core.execution.ExecutionElement.ExecutionElementType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.execution.ExecutionStatus;
import org.robotframework.ide.eclipse.main.plugin.execution.ExecutionStatus.Status;
import org.robotframework.ide.eclipse.main.plugin.execution.CollapseAllAction;
import org.robotframework.ide.eclipse.main.plugin.execution.ExecutionViewContentProvider;
import org.robotframework.ide.eclipse.main.plugin.execution.ExecutionViewLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.execution.ExpandAllAction;
import org.robotframework.red.graphics.ImagesManager;

public class ExecutionView {

    @Inject
    protected IEventBroker eventBroker;

    private StyledText styledText;

    private TreeViewer viewer;

    private List<ExecutionStatus> input = newArrayList();

    private CLabel passCounterLabel;

    private CLabel failCounterLabel;

    private int passCounter = 0;

    private int failCounter = 0;

    private LinkedList<ExecutionStatus> stack = new LinkedList<ExecutionStatus>();

    private ISelectionChangedListener viewerSelectionChangeListener;

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

        viewer = new TreeViewer(parent);
        viewer.getTree().setHeaderVisible(false);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTree());
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(viewer.getTree());
        ViewerColumnsFactory.newColumn("Status")
                .labelsProvidedBy(new ExecutionViewLabelProvider())
                .withMinWidth(400)
                .createFor(viewer);
        viewer.setContentProvider(new ExecutionViewContentProvider());
        viewer.setInput(input.toArray(new ExecutionStatus[input.size()]));
        viewerSelectionChangeListener = new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final ExecutionStatus status = (ExecutionStatus) ((TreeSelection) event.getSelection()).getFirstElement();
                if (status != null) {
                    final String message = status.getMessage();
                    if (message != null && !message.equals("")) {
                        styledText.setText(message);
                    } else {
                        styledText.setText("");
                    }
                }
            }
        };
        viewer.addSelectionChangedListener(viewerSelectionChangeListener);

        styledText = new StyledText(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        styledText.setFont(JFaceResources.getTextFont());
        GridDataFactory.fillDefaults().grab(true, false).indent(3, 0).hint(0, 50).applyTo(styledText);
        GridLayoutFactory.fillDefaults().applyTo(styledText);
        styledText.setEditable(false);
        styledText.setAlwaysShowScrollBars(false);

        ExpandAllAction expandAction = new ExpandAllAction(viewer);
        expandAction.setText("Expand All");
        expandAction.setImageDescriptor(RedImages.getExpandAllImage());
        part.getViewSite().getActionBars().getToolBarManager().add(expandAction);
        CollapseAllAction collapseAction = new CollapseAllAction(viewer);
        collapseAction.setText("Collapse All");
        collapseAction.setImageDescriptor(RedImages.getCollapseAllImage());
        part.getViewSite().getActionBars().getToolBarManager().add(collapseAction);
    }

    @Focus
    public void onFocus() {
        viewer.getControl().setFocus();
    }

    @PreDestroy
    public void dispose() {
        
    }

    @Inject
    @Optional
    private void executionEvent(@UIEventTopic("ExecutionView/ExecutionEvent") final ExecutionElement executionElement) {

        if (executionElement.getStatus() == null && executionElement.getType() == ExecutionElementType.SUITE) {
            ExecutionStatus status = new ExecutionStatus(executionElement.getName(), Status.RUNNING,
                    executionElement.getType(), new ArrayList<ExecutionStatus>(), null);
            if (stack.isEmpty()) {
                stack.add(status);
                input.add(status);
                viewer.setInput(input.toArray(new ExecutionStatus[input.size()]));
            } else {
                ExecutionStatus last = stack.getLast();
                status.setParent(last);
                last.addChildren(status);
                stack.addLast(status);
            }
            viewer.expandToLevel(status, 1);
        }
        if (executionElement.getStatus() == null && executionElement.getType() == ExecutionElementType.TEST) {
            ExecutionStatus status = new ExecutionStatus(executionElement.getName(), Status.RUNNING,
                    executionElement.getType(), new ArrayList<ExecutionStatus>(), null);
            ExecutionStatus last = stack.getLast();
            status.setParent(last);
            last.addChildren(status);
            viewer.expandToLevel(status, 1);
        }

        if (executionElement.getStatus() != null && executionElement.getType() == ExecutionElementType.SUITE) {
            ExecutionStatus last = stack.getLast();
            final int elapsedTime = executionElement.getElapsedTime();
            last.setElapsedTime(String.valueOf(((double) elapsedTime) / 1000));
            Status status = getStatus(executionElement);
            last.setStatus(status);
            if (stack.size() > 1) {
                stack.removeLast();
                if (status == Status.PASS) {
                    viewer.collapseToLevel(last, TreeViewer.ALL_LEVELS);
                }
            }
        }
        if (executionElement.getStatus() != null && executionElement.getType() == ExecutionElementType.TEST) {
            ExecutionStatus last = stack.getLast();
            List<ExecutionStatus> list = last.getChildren();
            Status status = getStatus(executionElement);
            final int elapsedTime = executionElement.getElapsedTime();
            for (ExecutionStatus executionStatus : list) {
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

        refreshViewer();
    }

    @Inject
    @Optional
    private void clearEvent(@UIEventTopic("ExecutionView/ClearEvent") final String s) {
        stack.clear();
        input.clear();
        passCounter = 0;
        failCounter = 0;
        viewer.refresh();
    }

    private Status getStatus(ExecutionElement executionElement) {
        return executionElement.getStatus().equals("PASS") ? Status.PASS : Status.FAIL;
    }

    private void refreshViewer() {
        passCounterLabel.setText("Passed: " + passCounter);
        failCounterLabel.setText("Failed: " + failCounter);
        viewer.refresh();
    }

}
