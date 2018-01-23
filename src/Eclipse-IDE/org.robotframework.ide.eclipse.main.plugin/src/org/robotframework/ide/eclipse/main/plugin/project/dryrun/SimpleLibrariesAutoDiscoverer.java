/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class SimpleLibrariesAutoDiscoverer extends LibrariesAutoDiscoverer {

    private final RobotSuiteFile suite;

    private final String libraryNameToDiscover;

    public SimpleLibrariesAutoDiscoverer(final RobotProject robotProject, final RobotSuiteFile suite,
            final String libraryNameToDiscover, final Consumer<Collection<RobotDryRunLibraryImport>> summaryHandler) {
        super(robotProject, summaryHandler);
        this.suite = suite;
        this.libraryNameToDiscover = libraryNameToDiscover;
    }

    @Override
    void prepareDiscovering(final IProgressMonitor monitor) throws CoreException {
        // nothing to do
    }

    @Override
    void startDiscovering(final IProgressMonitor monitor) throws InterruptedException, CoreException {
        final Set<String> libraryNames = newHashSet(libraryNameToDiscover);
        startDryRunDiscovering(monitor, libraryNames);
    }

    @Override
    List<RobotDryRunLibraryImport> getImportedLibraries() {
        final List<RobotDryRunLibraryImport> importedLibraries = super.getImportedLibraries();
        importedLibraries.forEach(libImport -> setImportersPaths(libImport, newArrayList(suite)));
        return importedLibraries;
    }

}
