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
import java.util.Set;

import javax.inject.Named;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.StaticAnalysisHandler.E4StaticAnalysisHandler;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class StaticAnalysisHandler extends DIParameterizedHandler<E4StaticAnalysisHandler> {

    public StaticAnalysisHandler() {
        super(E4StaticAnalysisHandler.class);
    }

    public static class E4StaticAnalysisHandler {

        @Execute
        public Object runStaticAnalysis(final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final List<IResource> selectedResources = Selections.getAdaptableElements(selection, IResource.class);

            for (final RobotSuiteFile suiteFile : RobotSuiteFileCollector.collectFiles(selectedResources)) {
                RobotArtifactsValidator.revalidate(suiteFile);
            }

            return null;
        }
    }

    static class RobotSuiteFileCollector {

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
