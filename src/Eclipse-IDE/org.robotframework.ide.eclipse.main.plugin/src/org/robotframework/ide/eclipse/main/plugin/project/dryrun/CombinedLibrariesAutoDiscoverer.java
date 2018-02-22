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

    private final ExternalLibrariesImportCollector referenceLibraryImportCollector;

    public CombinedLibrariesAutoDiscoverer(final RobotProject robotProject, final Collection<RobotSuiteFile> suites,
            final Consumer<Collection<RobotDryRunLibraryImport>> summaryHandler) {
        super(robotProject, summaryHandler);
        this.suites = suites;
        this.referenceLibraryImportCollector = new ExternalLibrariesImportCollector(robotProject);
    }

    @Override
    void prepareDiscovering(final IProgressMonitor monitor) throws CoreException {
        referenceLibraryImportCollector.collectFromSuites(suites, monitor);
    }

    @Override
    void startDiscovering(final IProgressMonitor monitor) throws InterruptedException, CoreException {
        final Set<String> libraryNames = referenceLibraryImportCollector.getUnknownLibraryNames().keySet();
        if (!libraryNames.isEmpty()) {
            startDryRunDiscovering(monitor, libraryNames);
        }
    }

    @Override
    List<RobotDryRunLibraryImport> getImportedLibraries() {
        final List<RobotDryRunLibraryImport> importedLibraries = super.getImportedLibraries();
        importedLibraries.forEach(libImport -> setImportersPaths(libImport,
                referenceLibraryImportCollector.getUnknownLibraryNames().get(libImport.getName())));

        final Set<RobotDryRunLibraryImport> collectedLibraries = referenceLibraryImportCollector.getLibraryImports();
        collectedLibraries.forEach(libImport -> setImportersPaths(libImport,
                referenceLibraryImportCollector.getLibraryImporters().get(libImport)));

        importedLibraries.addAll(collectedLibraries);
        return importedLibraries;
    }

}
