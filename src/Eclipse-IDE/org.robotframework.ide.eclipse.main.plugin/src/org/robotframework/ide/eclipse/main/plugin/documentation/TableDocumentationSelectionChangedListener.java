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
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.views.DocumentationView;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class TableDocumentationSelectionChangedListener implements ISelectionChangedListener {

    private DocumentationView view;

    private DocViewUpdateJob updateJob = new DocViewUpdateJob("Documentation View Update Job");

    private RobotElement currentElementParent;

    public TableDocumentationSelectionChangedListener(final DocumentationView view) {
        this.view = view;
    }

    @Override
    public void selectionChanged(final SelectionChangedEvent event) {

        if (event != null && event.getSelection() instanceof IStructuredSelection) {
            final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            final Optional<RobotFileInternalElement> selectedElement = Selections.getOptionalFirstElement(selection,
                    RobotFileInternalElement.class);
            if (selectedElement.isPresent() && selection.size() == 1) {

                if (updateJob.getState() == Job.SLEEPING) {
                    updateJob.cancel();
                }

                final RobotFileInternalElement robotFileInternalElement = selectedElement.get();
                final ModelType modelType = ((AModelElement<?>) robotFileInternalElement.getLinkedElement())
                        .getModelType();

                final DocViewUpdateType docViewUpdateType = chooseDocViewUpdateType(robotFileInternalElement,
                        modelType);
                scheduleUpdate(robotFileInternalElement, docViewUpdateType);
            }
        }
    }

    private DocViewUpdateType chooseDocViewUpdateType(final RobotFileInternalElement robotFileInternalElement,
            final ModelType modelType) {
        if (modelType == ModelType.USER_KEYWORD_DOCUMENTATION || modelType == ModelType.TEST_CASE_DOCUMENTATION) {
            currentElementParent = robotFileInternalElement.getParent();
            return DocViewUpdateType.SETTING;
        } else {
            if (view.hasShowLibdocEnabled() && (modelType == ModelType.USER_KEYWORD_EXECUTABLE_ROW
                    || modelType == ModelType.TEST_CASE_EXECUTABLE_ROW)) {
                currentElementParent = null;
                return DocViewUpdateType.LIBDOC;
            } else {

                final RobotElement parent = modelType == ModelType.USER_KEYWORD || modelType == ModelType.TEST_CASE
                        ? robotFileInternalElement : robotFileInternalElement.getParent();
                if (parent != null && currentElementParent != parent) {
                    currentElementParent = parent;
                    return DocViewUpdateType.PARENT;
                }
            }
        }
        return DocViewUpdateType.UNKNOWN;
    }

    private void scheduleUpdate(final RobotFileInternalElement robotFileInternalElement,
            final DocViewUpdateType docUpdateType) {
        if (docUpdateType != DocViewUpdateType.UNKNOWN) {
            updateJob.setRobotFileInternalElement(robotFileInternalElement);
            updateJob.setDocUpdateType(docUpdateType);
            updateJob.schedule(DocViewUpdateJob.DOCVIEW_UPDATE_JOB_DELAY);
        }
    }

    class DocViewUpdateJob extends Job {

        public static final int DOCVIEW_UPDATE_JOB_DELAY = 500;

        private DocViewUpdateType docViewUpdateType;
        
        private RobotFileInternalElement robotFileInternalElement;

        public DocViewUpdateJob(final String name) {
            super(name);
            setSystem(true);
        }

        @Override
        protected IStatus run(final IProgressMonitor monitor) {
            if (docViewUpdateType == DocViewUpdateType.SETTING) {
                view.showDocumentation(robotFileInternalElement);
            } else if (docViewUpdateType == DocViewUpdateType.LIBDOC) {
                view.showLibdoc(robotFileInternalElement);
            } else if (docViewUpdateType == DocViewUpdateType.PARENT && currentElementParent != null) {
                final RobotCodeHoldingElement codeHoldingElement = (RobotCodeHoldingElement) currentElementParent;
                final RobotDefinitionSetting docSettingFromParent = codeHoldingElement.findSetting("Documentation");
                view.showDocumentation(docSettingFromParent);
            }
            return Status.OK_STATUS;
        }

        public void setDocUpdateType(final DocViewUpdateType docViewUpdateType) {
            this.docViewUpdateType = docViewUpdateType;
        }
        
        public void setRobotFileInternalElement(final RobotFileInternalElement robotFileInternalElement) {
            this.robotFileInternalElement = robotFileInternalElement;
        }
    }

    private enum DocViewUpdateType {
        SETTING,
        LIBDOC,
        PARENT,
        UNKNOWN
    }
}
