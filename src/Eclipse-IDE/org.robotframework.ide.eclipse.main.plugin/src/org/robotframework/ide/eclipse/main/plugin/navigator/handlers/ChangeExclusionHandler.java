/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.Collection;
import java.util.List;

import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.services.IEvaluationService;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigWriter;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditor;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.propertytester.RedXmlForNavigatorPropertyTester;
import org.robotframework.red.viewers.Selections;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Michal Anglart
 *
 */
abstract class ChangeExclusionHandler {

    @Execute
    public Object changeExclusion(final IWorkbenchPartSite site, final IEventBroker eventBroker,
            final @Named(Selections.SELECTION) IStructuredSelection selection) {
        final List<IFolder> foldersToChange = Selections.getElements(selection, IFolder.class);
        final Multimap<IProject, IPath> groupedPaths = groupByProject(foldersToChange);

        for (final IProject groupingProject : groupedPaths.keySet()) {
            changeExclusion(groupingProject, groupedPaths.get(groupingProject));

            eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_VALIDATION_EXCLUSIONS_STRUCTURE_CHANGED,
                    groupedPaths.get(groupingProject));

            final IEvaluationService evalService = site.getService(IEvaluationService.class);
            evalService.requestEvaluation(RedXmlForNavigatorPropertyTester.PROPERTY_IS_INCLUDED);
            evalService.requestEvaluation(RedXmlForNavigatorPropertyTester.PROPERTY_IS_EXCLUDED);
        }
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

        final IEditorPart editor = findEditorIfAlreadyOpened(robotProject);
        final RedProjectEditorInput redProjectInput = getEditorInput(editor);

        final RobotProjectConfig config = provideConfig(redProjectInput, robotProject.getConfigurationFile());

        for (final IPath pathToChange : toChange) {
            changeExclusion(config, pathToChange);
        }

        if (redProjectInput == null) {
            new RobotProjectConfigWriter().writeConfiguration(config, project);
        }
    }

    protected abstract void changeExclusion(RobotProjectConfig config, IPath pathToChange);

    private IEditorPart findEditorIfAlreadyOpened(final RobotProject robotProject) {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final FileEditorInput input = new FileEditorInput(robotProject.getConfigurationFile());
        return page.findEditor(input);
    }

    private RedProjectEditorInput getEditorInput(final IEditorPart editor) {
        return editor instanceof RedProjectEditor ? ((RedProjectEditor) editor).getRedProjectEditorInput() : null;
    }

    private RobotProjectConfig provideConfig(final RedProjectEditorInput redProjectInput, final IFile externalFile) {
        if (redProjectInput == null) {
            return new RobotProjectConfigReader().readConfiguration(externalFile);
        } else {
            return redProjectInput.getProjectConfiguration();
        }
    }
}
