/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.ExcludedResources;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Multimaps;

class RobotSuiteFileCollector {

    static Map<RobotProject, Collection<RobotSuiteFile>> collectGroupedByProject(final Collection<IResource> resources,
            final IProgressMonitor monitor) {
        final Set<RobotSuiteFile> files = collectFiles(resources, monitor);
        return Multimaps.index(files, file -> file.getProject()).asMap();
    }

    @VisibleForTesting
    static Set<RobotSuiteFile> collectFiles(final Collection<IResource> resources, final IProgressMonitor monitor) {
        final Set<RobotSuiteFile> files = new HashSet<>();

        for (final IResource resource : resources) {
            if (monitor.isCanceled()) {
                break;
            }

            if (shouldBeSkipped(resource)) {
                continue;
            }

            if (resource.getType() == IResource.FILE) {
                final RobotSuiteFile suiteFile = RedPlugin.getModelManager().createSuiteFile((IFile) resource);
                if (suiteFile.isSuiteFile() || suiteFile.isResourceFile() || suiteFile.isInitializationFile()) {
                    files.add(suiteFile);
                }
            } else if (resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT) {
                files.addAll(collectNestedFiles((IContainer) resource, monitor));
            }
        }

        return files;
    }

    private static boolean shouldBeSkipped(final IResource resource) {
        final RobotProject robotProject = RedPlugin.getModelManager().createProject(resource.getProject());
        final RobotProjectConfig projectConfig = robotProject.getRobotProjectConfig();
        return projectConfig == null || ExcludedResources.isHiddenInEclipse(resource)
                || ExcludedResources.isInsideExcludedPath(resource, projectConfig)
                || resource.getType() == IResource.FILE
                        && !ExcludedResources.hasRequiredSize((IFile) resource, projectConfig);
    }

    private static Set<RobotSuiteFile> collectNestedFiles(final IContainer container, final IProgressMonitor monitor) {
        try {
            return collectFiles(Arrays.asList(container.members()), monitor);
        } catch (final CoreException e) {
            return Collections.emptySet();
        }
    }
}
