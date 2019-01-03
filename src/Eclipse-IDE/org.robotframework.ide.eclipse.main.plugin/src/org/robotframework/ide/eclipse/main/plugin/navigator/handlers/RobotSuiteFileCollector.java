/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.ExcludedResources;

class RobotSuiteFileCollector {

    static WorkspaceJob createCollectingJob(final List<IResource> resources,
            final Consumer<List<RobotSuiteFile>> suitesConsumer) {
        return new WorkspaceJob("Collecting robot suites") {

            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                final List<RobotSuiteFile> suites = RobotSuiteFileCollector.collectFiles(resources, monitor);

                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }

                suitesConsumer.accept(suites);

                return Status.OK_STATUS;
            }
        };
    }

    private static List<RobotSuiteFile> collectFiles(final List<IResource> resources, final IProgressMonitor monitor) {
        final Set<RobotSuiteFile> files = new LinkedHashSet<>();

        for (final IResource resource : resources) {
            if (monitor.isCanceled()) {
                break;
            }

            if (shouldBeSkipped(resource)) {
                continue;
            }

            if (resource.getType() == IResource.FILE) {
                final RobotSuiteFile suiteFile = RedPlugin.getModelManager().createSuiteFile((IFile) resource);
                if (suiteFile.isSuiteFile() || suiteFile.isRpaSuiteFile() || suiteFile.isResourceFile()
                        || suiteFile.isInitializationFile()) {
                    files.add(suiteFile);
                }
            } else if (resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT) {
                files.addAll(collectNestedFiles((IContainer) resource, monitor));
            }
        }

        return new ArrayList<>(files);
    }

    private static boolean shouldBeSkipped(final IResource resource) {
        final RobotProject robotProject = RedPlugin.getModelManager().createProject(resource.getProject());
        final RobotProjectConfig projectConfig = robotProject.getRobotProjectConfig();
        return ExcludedResources.isHiddenInEclipse(resource)
                || ExcludedResources.isInsideExcludedPath(resource, projectConfig)
                || resource.getType() == IResource.FILE && !ExcludedResources.hasRequiredSize((IFile) resource, projectConfig);
    }

    private static List<RobotSuiteFile> collectNestedFiles(final IContainer container, final IProgressMonitor monitor) {
        try {
            return collectFiles(Arrays.asList(container.members()), monitor);
        } catch (final CoreException e) {
            return Collections.emptyList();
        }
    }
}
