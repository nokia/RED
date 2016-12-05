/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidatorConfig;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidatorConfigFactory;

class RobotFormEditorPartListener implements IPartListener {

    private static final int REVALIDATE_JOB_DELAY = 2000;

    @Override
    public void partOpened(IWorkbenchPart part) {
    }

    @Override
    public void partDeactivated(IWorkbenchPart part) {
    }

    @Override
    public void partClosed(IWorkbenchPart part) {
    }

    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
        if (part instanceof RobotFormEditor) {
            final RobotFormEditor editor = (RobotFormEditor) part;
            final RobotSuiteFile suiteModel = editor.provideSuiteModel();
            if (suiteModel.getParent() != null) {
                final IProject project = suiteModel.getProject().getProject();
                final List<IFile> files = Collections.singletonList(suiteModel.getFile());
                final ModelUnitValidatorConfig validatorConfig = ModelUnitValidatorConfigFactory.create(files);
                final Job validationJob = RobotArtifactsValidator.createValidationJob(project, validatorConfig);
                validationJob.schedule(REVALIDATE_JOB_DELAY);
            }
        }
    }

    @Override
    public void partActivated(IWorkbenchPart part) {
    }
}
