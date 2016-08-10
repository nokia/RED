/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.documentation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.views.DocumentationView;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class TableDocumentationSelectionChangedListener implements ISelectionChangedListener {

    private DocumentationView view;

    private DocViewUpdateJob updateJob = new DocViewUpdateJob("Documentation View Update Job");

    public TableDocumentationSelectionChangedListener(DocumentationView view) {
        this.view = view;
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

                if (modelType == ModelType.USER_KEYWORD_DOCUMENTATION
                        || modelType == ModelType.TEST_CASE_DOCUMENTATION) {
                    showDoc(robotFileInternalElement);
                }
            }
        }

    }

    private void showDoc(final RobotFileInternalElement robotFileInternalElement) {
        updateJob.setRobotFileInternalElement(robotFileInternalElement);
        updateJob.schedule();
    }

    class DocViewUpdateJob extends Job {

        private RobotFileInternalElement robotFileInternalElement;

        public DocViewUpdateJob(final String name) {
            super(name);
            setSystem(true);
        }

        @Override
        protected IStatus run(final IProgressMonitor monitor) {
            view.showDocumentation(robotFileInternalElement);
            return Status.OK_STATUS;
        }

        public void setRobotFileInternalElement(final RobotFileInternalElement robotFileInternalElement) {
            this.robotFileInternalElement = robotFileInternalElement;
        }
    }
}
