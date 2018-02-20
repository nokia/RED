/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
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
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.robotframework.ide.eclipse.main.plugin.model.LibraryDescriptor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

/**
 * @author bembenek
 */
public class KeywordsAutoDiscoverer extends AbstractAutoDiscoverer {

    private final RobotDryRunKeywordSourceCollector dryRunLKeywordSourceCollector;

    public KeywordsAutoDiscoverer(final RobotProject robotProject) {
        super(robotProject);
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
    void startDiscovering(final IProgressMonitor monitor) throws InterruptedException, CoreException {
        final Set<String> libraryNames = robotProject.getLibraryDescriptorsStream()
                .map(LibraryDescriptor::getName)
                .collect(toSet());
        startDryRunDiscovering(monitor, libraryNames);
    }

    @Override
    RobotDryRunKeywordEventListener createDryRunCollectorEventListener(final Consumer<String> libNameHandler) {
        return new RobotDryRunKeywordEventListener(dryRunLKeywordSourceCollector, libNameHandler);
    }

    @Override
    void startDryRunClient(final int port, final File dataSource) throws CoreException {
        final EnvironmentSearchPaths additionalPaths = new EnvironmentSearchPaths(robotProject.getClasspath(),
                robotProject.getPythonpath());

        robotProject.getRuntimeEnvironment().startKeywordAutoDiscovering(port, dataSource, additionalPaths);
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

}
