/*
 * Copyright 2015-2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.navigator.RobotValidationExcludedDecorator;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.RevalidateSelectionHandler.RobotSuiteFileCollector;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigWriter;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidatorConfig;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidatorConfigFactory;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.Selections;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Michal Anglart
 *
 */
abstract class ChangeExclusionHandler {

    private static final long REVALIDATE_JOB_DELAY = 2000;

    public void changeExclusion(final IEventBroker eventBroker, final IStructuredSelection selection) {
        final List<IResource> resourcesToChange = Selections.getAdaptableElements(selection, IResource.class);
        final Map<IProject, Collection<RobotSuiteFile>> grouped = RobotSuiteFileCollector
                .collectGroupedByProject(resourcesToChange);
        final Multimap<IProject, IPath> groupedPaths = groupByProject(resourcesToChange);

        for (final IProject groupingProject : groupedPaths.keySet()) {
            changeExclusion(groupingProject, groupedPaths.get(groupingProject));

            final RedProjectConfigEventData<Collection<IPath>> eventData = new RedProjectConfigEventData<>(
                    groupingProject.getFile(RobotProjectConfig.FILENAME), groupedPaths.get(groupingProject));
            eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_VALIDATION_EXCLUSIONS_STRUCTURE_CHANGED, eventData);
            final Collection<RobotSuiteFile> suiteModels = grouped.get(groupingProject);
            final ModelUnitValidatorConfig validatorConfig = ModelUnitValidatorConfigFactory.create(suiteModels);
            final Job validationJob = RobotArtifactsValidator.createValidationJob(groupingProject, validatorConfig);
            validationJob.schedule(REVALIDATE_JOB_DELAY);

        }
       
        SwtThread.asyncExec(new Runnable() {
            @Override
            public void run() {
                final IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();
                manager.update(RobotValidationExcludedDecorator.ID);

            }
        });
      
    }

    private Multimap<IProject, IPath> groupByProject(final List<IResource> resourcesToChange) {
        final Multimap<IProject, IPath> groupedPaths = LinkedListMultimap.create();
        for (final IResource resource : resourcesToChange) {
            groupedPaths.put(resource.getProject(), resource.getProjectRelativePath());
        }
        return groupedPaths;
    }

    private void changeExclusion(final IProject project, final Collection<IPath> toChange) {
        final RobotProject robotProject = RedPlugin.getModelManager().createProject(project);
        RobotProjectConfig config = robotProject.getOpenedProjectConfig();

        final boolean inEditor = config != null;
        if (config == null) {
            config = new RedEclipseProjectConfigReader().readConfiguration(robotProject.getConfigurationFile());
        }

        for (final IPath pathToChange : toChange) {
            changeExclusion(config, pathToChange);
        }

        if (!inEditor) {
            new RedEclipseProjectConfigWriter().writeConfiguration(config, project);
        }
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (final CoreException e) {
            // nothing to do
        }
    }

    protected abstract void changeExclusion(RobotProjectConfig config, IPath pathToChange);
}
