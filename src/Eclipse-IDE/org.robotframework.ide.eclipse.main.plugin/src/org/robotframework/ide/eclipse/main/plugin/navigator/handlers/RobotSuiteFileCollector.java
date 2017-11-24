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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Multimaps;

class RobotSuiteFileCollector {

    static Map<IProject, Collection<RobotSuiteFile>> collectGroupedByProject(final Collection<IResource> resources) {
        final Set<RobotSuiteFile> files = collectFiles(resources);
        return Multimaps.index(files, file -> file.getProject().getProject()).asMap();
    }

    @VisibleForTesting
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
        } catch (final CoreException e) {
            return Collections.emptySet();
        }
    }
}
