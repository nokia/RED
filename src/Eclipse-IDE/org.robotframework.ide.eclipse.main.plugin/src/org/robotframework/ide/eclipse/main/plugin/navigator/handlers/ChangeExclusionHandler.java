/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.navigator.RobotValidationExcludedDecorator;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigWriter;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.Selections;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Michal Anglart
 *
 */
abstract class ChangeExclusionHandler {

    public Object changeExclusion(final IEventBroker eventBroker, final IStructuredSelection selection) {
        final List<IFolder> foldersToChange = Selections.getElements(selection, IFolder.class);
        final Multimap<IProject, IPath> groupedPaths = groupByProject(foldersToChange);

        for (final IProject groupingProject : groupedPaths.keySet()) {
            changeExclusion(groupingProject, groupedPaths.get(groupingProject));

            final RedProjectConfigEventData<Collection<IPath>> eventData = new RedProjectConfigEventData<>(
                    groupingProject.getFile(RobotProjectConfig.FILENAME), groupedPaths.get(groupingProject));
            eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_VALIDATION_EXCLUSIONS_STRUCTURE_CHANGED, eventData);

        }
        SwtThread.asyncExec(new Runnable() {
            @Override
            public void run() {
                final IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();
                manager.update(RobotValidationExcludedDecorator.ID);
            }
        });
        return null;
    }

    private Multimap<IProject, IPath> groupByProject(final List<IFolder> foldersToChange) {
        final Multimap<IProject, IPath> groupedPaths = LinkedListMultimap.create();
        for (final IFolder folder : foldersToChange) {
            groupedPaths.put(folder.getProject(), folder.getProjectRelativePath());
        }
        return groupedPaths;
    }

    private void changeExclusion(final IProject project, final Collection<IPath> toChange) {
        final RobotProject robotProject = RedPlugin.getModelManager().createProject(project);
        RobotProjectConfig config = robotProject.getOpenedProjectConfig();

        final boolean inEditor = config != null;
        if (config == null) {
            config = new RobotProjectConfigReader().readConfiguration(robotProject.getConfigurationFile());
        }

        for (final IPath pathToChange : toChange) {
            changeExclusion(config, pathToChange);
        }

        if (!inEditor) {
            new RobotProjectConfigWriter().writeConfiguration(config, project);
        }
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (final CoreException e) {
            // nothing to do
        }
    }

    protected abstract void changeExclusion(RobotProjectConfig config, IPath pathToChange);
}
