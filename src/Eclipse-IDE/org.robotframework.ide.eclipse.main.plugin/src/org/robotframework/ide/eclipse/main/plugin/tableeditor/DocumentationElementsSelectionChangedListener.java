/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.views.DocumentationView;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

/**
 * @author mmarzec
 */
public class DocumentationElementsSelectionChangedListener implements ISelectionChangedListener {

    private IEventBroker eventBroker;

    private boolean hasDocViewInitialized;

    public DocumentationElementsSelectionChangedListener(final IEventBroker eventBroker) {
        this.eventBroker = eventBroker;
    }

    @Override
    public void selectionChanged(final SelectionChangedEvent event) {

        if (event != null && event.getSelection() instanceof IStructuredSelection) {
            final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            final Optional<RobotFileInternalElement> selectedElement = Selections.getOptionalFirstElement(selection,
                    RobotFileInternalElement.class);
            if (selectedElement.isPresent() && selection.size() == 1) {
                final RobotFileInternalElement robotFileInternalElement = selectedElement.get();
                final ModelType modelType = ((AModelElement<?>) robotFileInternalElement.getLinkedElement())
                        .getModelType();

                if (modelType == ModelType.USER_KEYWORD_DOCUMENTATION) {
                    postEvent(robotFileInternalElement);
                } else if (modelType == ModelType.TEST_CASE_DOCUMENTATION) {
                    postEvent(robotFileInternalElement);
                }
            }
        }

    }

    private void postEvent(final RobotFileInternalElement robotFileInternalElement) {
        initDocumentationView();
        eventBroker.post(DocumentationView.SHOW_DOC_EVENT_TOPIC, robotFileInternalElement);
    }

    private void initDocumentationView() {
        if (!hasDocViewInitialized) {
            final IWorkbench workbench = PlatformUI.getWorkbench();
            workbench.getDisplay().syncExec(new Runnable() {

                @Override
                public void run() {
                    final IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
                    if (activeWorkbenchWindow != null) {
                        final IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
                        if (page != null) {
                            final IViewPart docViewPart = page.findView(DocumentationView.ID);
                            if (docViewPart == null || !page.isPartVisible(docViewPart)) {
                                try {
                                    page.showView(DocumentationView.ID);
                                } catch (final PartInitException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
            hasDocViewInitialized = true;
        }
    }

}
