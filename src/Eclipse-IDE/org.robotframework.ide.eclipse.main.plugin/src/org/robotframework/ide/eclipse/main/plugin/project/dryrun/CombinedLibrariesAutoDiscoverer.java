/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class CombinedLibrariesAutoDiscoverer extends LibrariesAutoDiscoverer {

    private final Collection<RobotSuiteFile> suites;

    private final ExternalLibrariesImportCollector libImportCollector;

    public CombinedLibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<RobotSuiteFile> suites,
            final Consumer<Collection<RobotDryRunLibraryImport>> summaryHandler) {
        super(robotProject, summaryHandler);
        this.suites = suites;
        this.libImportCollector = new ExternalLibrariesImportCollector(robotProject);
    }

    @Override
    void prepareDiscovering(final IProgressMonitor monitor) throws CoreException {
        libImportCollector.collectFromSuites(suites, monitor);
    }

    @Override
    void startDiscovering(final IProgressMonitor monitor) throws InterruptedException, CoreException {
        final Set<String> libraryNames = libImportCollector.getUnknownLibraryNames().keySet();
        if (!libraryNames.isEmpty()) {
            startDryRunDiscovering(monitor, libraryNames);
        }
    }

    @Override
    List<RobotDryRunLibraryImport> getImportedLibraries() {
        final List<RobotDryRunLibraryImport> importedLibraries = super.getImportedLibraries();
        importedLibraries.forEach(libImport -> setImportersPaths(libImport,
                libImportCollector.getUnknownLibraryNames().get(libImport.getName())));

        final Set<RobotDryRunLibraryImport> collectedLibraries = libImportCollector.getLibraryImports();
        collectedLibraries.forEach(
                libImport -> setImportersPaths(libImport, libImportCollector.getLibraryImporters().get(libImport)));

        importedLibraries.addAll(collectedLibraries);
        return importedLibraries;
    }

}
