/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.dryrun.RobotDryRunKeywordEventListener;
import org.rf.ide.core.dryrun.RobotDryRunKeywordSource;
import org.rf.ide.core.dryrun.RobotDryRunKeywordSourceCollector;
import org.rf.ide.core.dryrun.RobotDryRunTemporarySuites;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

import com.google.common.io.Files;

/**
 * @author bembenek
 */
public class KeywordsAutoDiscoverer extends AbstractAutoDiscoverer {

    private final RobotDryRunKeywordSourceCollector dryRunLKeywordSourceCollector;

    public KeywordsAutoDiscoverer(final RobotProject robotProject) {
        super(robotProject, new ArrayList<>(), new LibrariesSourcesCollector(robotProject),
                new DryRunTargetsCollector());
        this.dryRunLKeywordSourceCollector = new RobotDryRunKeywordSourceCollector();
    }

    @Override
    RobotDryRunKeywordEventListener createDryRunCollectorEventListener(final Consumer<String> startSuiteHandler) {
        return new RobotDryRunKeywordEventListener(dryRunLKeywordSourceCollector, startSuiteHandler);
    }

    @Override
    Job start(final Shell parent) {
        if (lockDryRun()) {
            try {
                new ProgressMonitorDialog(parent).run(true, true, monitor -> {
                    try {
                        startDiscovering(monitor);
                        if (monitor.isCanceled()) {
                            return;
                        }
                        startAddingKeywordsToProject(monitor, dryRunLKeywordSourceCollector.getKeywordSources());
                    } catch (final CoreException e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.done();
                        unlockDryRun();
                    }
                });
            } catch (final InvocationTargetException e) {
                throw new AutoDiscovererException("Problems occurred during discovering keywords.", e);
            } catch (final InterruptedException e) {
                stopDiscovering();
            }
        }
        return null;
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
                final List<IFile> suites) {
            final List<String> libraryNames = collectLibraryNames(robotProject);
            if (!libraryNames.isEmpty()) {
                final File tempSuiteFile = RobotDryRunTemporarySuites.createLibraryFile(libraryNames);
                if (tempSuiteFile != null) {
                    suiteNames.add(tempSuiteFile.getParentFile().getName() + "."
                            + Files.getNameWithoutExtension(tempSuiteFile.getPath()));
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
