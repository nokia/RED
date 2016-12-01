/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidatorConfig;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidatorConfigFactory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.RevalidateEditorHandler.E4RevalidateEditorHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class RevalidateEditorHandler extends DIParameterizedHandler<E4RevalidateEditorHandler> {

    public RevalidateEditorHandler() {
        super(E4RevalidateEditorHandler.class);
    }

    public static class E4RevalidateEditorHandler {

        private static final String REVALIDATE_EDITOR_COMMAND_MODE_PARAMETER = "org.robotframework.red.revalidateEditor.mode";

        @Execute
        public void revalidate(@Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile suiteModel,
                @Named(REVALIDATE_EDITOR_COMMAND_MODE_PARAMETER) final String mode) {

            final IProject project = suiteModel.getProject().getProject();
            final ModelUnitValidatorConfig validatorConfig = createValidatorConfig(suiteModel, Mode.valueOf(mode));
            final Job validationJob = RobotArtifactsValidator.createValidationJob(project, validatorConfig);
            validationJob.schedule();
        }

        private ModelUnitValidatorConfig createValidatorConfig(final RobotSuiteFile suiteModel, final Mode mode) {
            if (mode == Mode.PROJECT) {
                final IProject project = suiteModel.getProject().getProject();
                return ModelUnitValidatorConfigFactory.create(project);
            } else {
                final List<IFile> files = Collections.singletonList(suiteModel.getFile());
                return ModelUnitValidatorConfigFactory.create(files);
            }
        }
    }

    public static enum Mode {
        FILE,
        PROJECT;
    }
}
