/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.dryrun.RobotDryRunKeywordSource;
import org.rf.ide.core.dryrun.RobotDryRunTemporarySuites;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

import com.google.common.io.Files;

/**
 * @author bembenek
 */
public class KeywordsAutoDiscoverer extends AbstractAutoDiscoverer {

    public KeywordsAutoDiscoverer(final RobotProject robotProject) {
        super(robotProject, Collections.<IResource> emptyList(), new DryRunTargetsCollector());
    }

    @Override
    public void start(final Shell parent) {
        if (lockDryRun()) {
            try {
                new ProgressMonitorDialog(parent).run(true, true, new IRunnableWithProgress() {

                    @Override
                    public void run(final IProgressMonitor monitor)
                            throws InvocationTargetException, InterruptedException {
                        try {
                            startDiscovering(monitor);
                            if (monitor.isCanceled()) {
                                return;
                            }
                            startAddingKeywordsToProject(monitor, dryRunLKeywordSourceCollector.getKeywordSources());
                        } finally {
                            monitor.done();
                            unlockDryRun();
                        }
                    }
                });
            } catch (final InvocationTargetException e) {
                throw new AutoDiscovererException("Problems occurred during discovering keywords.", e);
            } catch (final InterruptedException e) {
                stopServer();
            }
        }
    }

    private void startAddingKeywordsToProject(final IProgressMonitor monitor,
            final List<RobotDryRunKeywordSource> dryRunKeywordSources) {
        if (!dryRunKeywordSources.isEmpty()) {
            final SubMonitor subMonitor = SubMonitor.convert(monitor);
            subMonitor.subTask("Adding keywords to project...");
            subMonitor.setWorkRemaining(dryRunKeywordSources.size());
            for (final RobotDryRunKeywordSource keywordSource : dryRunKeywordSources) {
                robotProject.addKeywordSource(keywordSource);
                subMonitor.worked(1);
            }
        }
    }

    private static class DryRunTargetsCollector implements IDryRunTargetsCollector {

        private final List<String> suiteNames = new ArrayList<>();

        private final List<File> additionalProjectsLocations = new ArrayList<>();

        @Override
        public void collectSuiteNamesAndAdditionalProjectsLocations(final RobotProject robotProject,
                final List<IResource> suiteFiles) {
            final List<String> libraryNames = collectLibraryNames(robotProject);
            if (!libraryNames.isEmpty()) {
                final File tempSuiteFile = RobotDryRunTemporarySuites.createLibraryFile(libraryNames);
                if (tempSuiteFile != null) {
                    suiteNames.add(Files.getNameWithoutExtension(tempSuiteFile.getPath()));
                    additionalProjectsLocations.add(tempSuiteFile.getParentFile());
                }
            }
        }

        private List<String> collectLibraryNames(final RobotProject robotProject) {
            final List<String> libraryNames = new ArrayList<>();
            libraryNames.addAll(robotProject.getStandardLibraries().keySet());
            for (final ReferencedLibrary referencedLibrary : robotProject.getReferencedLibraries().keySet()) {
                libraryNames.add(referencedLibrary.getName());
            }
            return libraryNames;
        }

        @Override
        public List<String> getSuiteNames() {
            return suiteNames;
        }

        @Override
        public List<File> getAdditionalProjectsLocations() {
            return additionalProjectsLocations;
        }
    }
}
