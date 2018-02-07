/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.red.viewers.Selections;

class TableDocumentationSelectionChangedListener implements ISelectionChangedListener {

    private final DocumentationView view;

    private final DocViewUpdateJob updateJob = new DocViewUpdateJob("Documentation View Update Job");

    private RobotElement currentElementParent;

    TableDocumentationSelectionChangedListener(final DocumentationView view) {
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
                final SelectionLayerAccessor selectionLayerAccessor = ((RobotFormEditor) (PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getActivePage().getActiveEditor())).getSelectionLayerAccessor();
                final PositionCoordinate[] positions = selectionLayerAccessor.getSelectedPositions();
                if (positions.length > 0) {
                    final String label = selectionLayerAccessor.getLabelFromCell(positions[0].getRowPosition(),
                            positions[0].getColumnPosition());
                    scheduleUpdate(robotFileInternalElement, label, docViewUpdateType);
                }
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

    private void scheduleUpdate(final RobotFileInternalElement robotFileInternalElement, final String label,
            final DocViewUpdateType docUpdateType) {
        if (docUpdateType != DocViewUpdateType.UNKNOWN) {
            updateJob.setRobotFileInternalElement(robotFileInternalElement);
            updateJob.setLabel(label);
            updateJob.setDocUpdateType(docUpdateType);
            updateJob.schedule(DocViewUpdateJob.DOCVIEW_UPDATE_JOB_DELAY);
        }
    }

    class DocViewUpdateJob extends Job {

        public static final int DOCVIEW_UPDATE_JOB_DELAY = 500;

        private DocViewUpdateType docViewUpdateType;

        private RobotFileInternalElement robotFileInternalElement;

        private String label;

        public DocViewUpdateJob(final String name) {
            super(name);
            setSystem(true);
        }

        @Override
        protected IStatus run(final IProgressMonitor monitor) {
            if (docViewUpdateType == DocViewUpdateType.SETTING) {
                view.showDocumentation(robotFileInternalElement);
            } else if (docViewUpdateType == DocViewUpdateType.LIBDOC) {
                view.showLibdoc(robotFileInternalElement, label);
            } else if (docViewUpdateType == DocViewUpdateType.PARENT && currentElementParent != null) {
                final RobotCodeHoldingElement<?> codeHoldingElement = (RobotCodeHoldingElement<?>) currentElementParent;
                final Optional<RobotDefinitionSetting> docSettingFromParent = codeHoldingElement
                        .findSetting(ModelType.TEST_CASE_DOCUMENTATION, ModelType.USER_KEYWORD_DOCUMENTATION);
                view.showDocumentation(docSettingFromParent.orElse(null));
            }
            return Status.OK_STATUS;
        }

        public void setDocUpdateType(final DocViewUpdateType docViewUpdateType) {
            this.docViewUpdateType = docViewUpdateType;
        }

        public void setRobotFileInternalElement(final RobotFileInternalElement robotFileInternalElement) {
            this.robotFileInternalElement = robotFileInternalElement;
        }

        public void setLabel(final String label) {
            this.label = label;
        }
    }

    private enum DocViewUpdateType {
        SETTING,
        LIBDOC,
        PARENT,
        UNKNOWN
    }
}
