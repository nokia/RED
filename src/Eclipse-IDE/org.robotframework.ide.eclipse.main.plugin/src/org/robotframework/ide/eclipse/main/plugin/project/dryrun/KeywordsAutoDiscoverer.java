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
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.rf.ide.core.dryrun.RobotDryRunKeywordEventListener;
import org.rf.ide.core.dryrun.RobotDryRunKeywordSource;
import org.rf.ide.core.dryrun.RobotDryRunKeywordSourceCollector;
import org.rf.ide.core.dryrun.RobotDryRunTemporarySuites;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

import com.google.common.io.Files;

/**
 * @author bembenek
 */
public class KeywordsAutoDiscoverer extends AbstractAutoDiscoverer {

    private final RobotDryRunKeywordSourceCollector dryRunLKeywordSourceCollector;

    public KeywordsAutoDiscoverer(final RobotProject robotProject) {
        super(robotProject, new DryRunTargetsCollector());
        this.dryRunLKeywordSourceCollector = new RobotDryRunKeywordSourceCollector();
    }

    @Override
    public Job start() {
        if (lockDryRun()) {
            try {
                new ProgressMonitorDialog(Display.getCurrent().getActiveShell()).run(true, true, monitor -> {
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

    @Override
    EnvironmentSearchPaths collectLibrarySources(final RobotRuntimeEnvironment runtimeEnvironment)
            throws CoreException {
        return new EnvironmentSearchPaths(robotProject.getClasspath(), robotProject.getPythonpath());
    }

    @Override
    RobotDryRunKeywordEventListener createDryRunCollectorEventListener(final Consumer<String> startSuiteHandler) {
        return new RobotDryRunKeywordEventListener(dryRunLKeywordSourceCollector, startSuiteHandler);
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

        private final List<String> dataSourcePaths = new ArrayList<>();

        @Override
        public void collectSuiteNamesAndDataSourcePaths(final RobotProject robotProject) {
            final List<String> libraryNames = collectLibraryNames(robotProject);
            final Optional<File> tempSuiteFile = RobotDryRunTemporarySuites.createLibraryImportFile(libraryNames);
            tempSuiteFile.ifPresent(file -> {
                suiteNames.add(file.getParentFile().getName() + "." + Files.getNameWithoutExtension(file.getPath()));
                dataSourcePaths.add(file.getParentFile().getAbsolutePath());
            });
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
        public List<String> getDataSourcePaths() {
            return dataSourcePaths;
        }
    }
}
