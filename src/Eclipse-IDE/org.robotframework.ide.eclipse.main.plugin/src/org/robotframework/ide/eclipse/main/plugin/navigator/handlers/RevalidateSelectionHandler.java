/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Named;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.RevalidateSelectionHandler.E4RevalidateSelectionHandler;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidatorConfig;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidatorConfigFactory;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Function;
import com.google.common.collect.Multimaps;

public class RevalidateSelectionHandler extends DIParameterizedHandler<E4RevalidateSelectionHandler> {

    public RevalidateSelectionHandler() {
        super(E4RevalidateSelectionHandler.class);
    }

    public static class E4RevalidateSelectionHandler {

        @Execute
        public void revalidate(final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final List<IResource> selectedResources = Selections.getAdaptableElements(selection, IResource.class);

            final Map<IProject, Collection<RobotSuiteFile>> grouped = RobotSuiteFileCollector
                    .collectGroupedByProject(selectedResources);
            for (Entry<IProject, Collection<RobotSuiteFile>> entry : grouped.entrySet()) {
                final IProject project = entry.getKey();
                final Collection<RobotSuiteFile> suiteModels = entry.getValue();
                final ModelUnitValidatorConfig validatorConfig = ModelUnitValidatorConfigFactory.create(suiteModels);
                final Job validationJob = RobotArtifactsValidator.createValidationJob(project, validatorConfig);
                validationJob.schedule();
            }
        }
    }

    static class RobotSuiteFileCollector {

        static Map<IProject, Collection<RobotSuiteFile>> collectGroupedByProject(final Collection<IResource> resources) {
            final Set<RobotSuiteFile> files = collectFiles(resources);
            return Multimaps.index(files, new Function<RobotSuiteFile, IProject>() {

                @Override
                public IProject apply(final RobotSuiteFile file) {
                    return file.getProject().getProject();
                }
            }).asMap();
        }

        static Set<RobotSuiteFile> collectFiles(final Collection<IResource> resources) {
            final Set<RobotSuiteFile> files = new HashSet<>();

            for (final IResource resource : resources) {
                if (resource.getType() == IResource.FILE) {
                    final RobotSuiteFile suiteFile = RedPlugin.getModelManager().createSuiteFile((IFile) resource);
                    if (suiteFile.isSuiteFile() || suiteFile.isResourceFile() || suiteFile.isInitializationFile()) {
                        files.add(suiteFile);
                    }
                } else if (resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT) {
                    files.addAll(collectNestedFiles((IContainer) resource));
                }
            }

            return files;
        }

        private static Set<RobotSuiteFile> collectNestedFiles(final IContainer container) {
            try {
                return collectFiles(Arrays.asList(container.members()));
            } catch (CoreException e) {
                return Collections.emptySet();
            }
        }
    }

}
