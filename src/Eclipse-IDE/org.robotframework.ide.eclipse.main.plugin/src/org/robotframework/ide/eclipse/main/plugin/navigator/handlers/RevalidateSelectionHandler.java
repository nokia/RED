/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.RevalidateSelectionHandler.E4RevalidateSelectionHandler;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidatorConfig;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidatorConfigFactory;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class RevalidateSelectionHandler extends DIParameterizedHandler<E4RevalidateSelectionHandler> {

    public RevalidateSelectionHandler() {
        super(E4RevalidateSelectionHandler.class);
    }

    public static class E4RevalidateSelectionHandler {

        @Execute
        public void revalidate(final @Named(Selections.SELECTION) IStructuredSelection selection) {
            RevalidateSelectionHandler.revalidate(selection, 0);
        }
    }

    static void revalidate(final IStructuredSelection selection, final long delay) {
        final List<IResource> selectedResources = Selections.getAdaptableElements(selection, IResource.class);

        final Map<RobotProject, Collection<RobotSuiteFile>> filesGroupedByProject = RobotSuiteFileCollector
                .collectGroupedByProject(selectedResources);
        filesGroupedByProject.forEach((robotProject, suiteModels) -> {
            final ModelUnitValidatorConfig validatorConfig = ModelUnitValidatorConfigFactory.create(suiteModels);
            final Job validationJob = RobotArtifactsValidator.createValidationJob(robotProject.getProject(),
                    validatorConfig);
            validationJob.schedule(delay);
        });
    }

}
