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
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;
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

            final Map<IProject, Collection<IFile>> grouped = RobotSuiteFileCollector
                    .collectGroupedByProject(selectedResources);
            for (Entry<IProject, Collection<IFile>> entry : grouped.entrySet()) {
                final IProject project = entry.getKey();
                final Collection<IFile> files = entry.getValue();
                final ModelUnitValidatorConfig validatorConfig = ModelUnitValidatorConfigFactory.create(files);
                final Job validationJob = new RobotArtifactsValidator(project, new BuildLogger())
                        .createValidationJob(null, validatorConfig);
                validationJob.schedule();
            }
        }
    }

    static class RobotSuiteFileCollector {

        static Map<IProject, Collection<IFile>> collectGroupedByProject(final Collection<IResource> resources) {
            final Set<IFile> files = collectFiles(resources);
            return Multimaps.index(files, new Function<IFile, IProject>() {

                @Override
                public IProject apply(final IFile file) {
                    return file.getProject();
                }
            }).asMap();
        }

        static Set<IFile> collectFiles(final Collection<IResource> resources) {
            final Set<IFile> files = new HashSet<>();

            for (final IResource resource : resources) {
                if (resource.getType() == IResource.FILE) {
                    final RobotSuiteFile suiteFile = RedPlugin.getModelManager().createSuiteFile((IFile) resource);
                    if (suiteFile.isSuiteFile() || suiteFile.isResourceFile() || suiteFile.isInitializationFile()) {
                        files.add((IFile) resource);
                    }
                } else if (resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT) {
                    files.addAll(collectNestedFiles((IContainer) resource));
                }
            }

            return files;
        }

        private static Set<IFile> collectNestedFiles(final IContainer container) {
            try {
                return collectFiles(Arrays.asList(container.members()));
            } catch (CoreException e) {
                return Collections.emptySet();
            }
        }
    }

}
