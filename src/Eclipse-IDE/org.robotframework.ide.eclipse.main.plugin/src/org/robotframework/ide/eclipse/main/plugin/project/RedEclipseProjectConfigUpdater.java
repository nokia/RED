/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryType;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.JarStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.JarStructureBuilder.JarClass;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PythonLibStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PythonLibStructureBuilder.PythonClass;

import com.google.common.base.Optional;

public class RedEclipseProjectConfigUpdater {

    private final RobotProjectConfig config;

    private final RobotProject project;

    private final List<ReferencedLibrary> addedLibs = new ArrayList<>();

    public RedEclipseProjectConfigUpdater(final RobotProjectConfig config, final RobotProject robotProject) {
        this.config = config;
        this.project = robotProject;
    }

    public void addLibrary(final RobotDryRunLibraryImport dryRunLibraryImport) {
        if (dryRunLibraryImport.getType() == DryRunLibraryType.JAVA) {
            addJavaLibrary(dryRunLibraryImport);
        } else {
            addPythonLibrary(dryRunLibraryImport);
        }
    }

    public List<ReferencedLibrary> getAddedLibraries() {
        return addedLibs;
    }

    private void addPythonLibrary(final RobotDryRunLibraryImport dryRunLibraryImport) {
        final PythonLibStructureBuilder pythonLibStructureBuilder = new PythonLibStructureBuilder(
                project.getRuntimeEnvironment(), project.getRobotProjectConfig(), project.getProject());
        Collection<PythonClass> pythonClasses = newArrayList();
        try {
            pythonClasses = pythonLibStructureBuilder.provideEntriesFromFile(dryRunLibraryImport.getSourcePath(),
                    Optional.of(dryRunLibraryImport.getName()), true);
        } catch (final RobotEnvironmentException e) {
            if (!isPythonLibraryRecognizedAndAddedByName(dryRunLibraryImport)) {
                dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED, e.getMessage());
            }
            return;
        }

        final Collection<ReferencedLibrary> librariesToAdd = new ArrayList<>();
        if (pythonClasses.isEmpty()) {
            dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED,
                    "RED was unable to find classes inside '" + dryRunLibraryImport.getSourcePath() + "' module.");
        } else {
            for (final PythonClass pythonClass : pythonClasses) {
                if (pythonClass.getQualifiedName().equalsIgnoreCase(dryRunLibraryImport.getName())) {
                    librariesToAdd.add(pythonClass.toReferencedLibrary(dryRunLibraryImport.getSourcePath()));
                }
            }
            if (librariesToAdd.isEmpty()) {
                dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED,
                        "RED was unable to find class '" + dryRunLibraryImport.getName() + "' inside '"
                                + dryRunLibraryImport.getSourcePath() + "' module.");
            }
        }

        addReferencedLibraries(dryRunLibraryImport, librariesToAdd);
    }

    private boolean isPythonLibraryRecognizedAndAddedByName(final RobotDryRunLibraryImport dryRunLibraryImport) {
        Optional<File> modulePath = Optional.absent();
        try {
            final EnvironmentSearchPaths envSearchPaths = new RedEclipseProjectConfig(config)
                    .createEnvironmentSearchPaths(project.getProject());
            modulePath = project.getRuntimeEnvironment().getModulePath(dryRunLibraryImport.getName(), envSearchPaths);
        } catch (final RobotEnvironmentException e1) {
            // that's fine
        }
        if (modulePath.isPresent()) {
            final Path path = new Path(modulePath.get().getPath());
            final ReferencedLibrary newLibrary = ReferencedLibrary.create(LibraryType.PYTHON,
                    dryRunLibraryImport.getName(), path.toPortableString());
            final Collection<ReferencedLibrary> librariesToAdd = new ArrayList<>();
            librariesToAdd.add(newLibrary);
            addReferencedLibraries(dryRunLibraryImport, librariesToAdd);
            return true;
        }
        return false;
    }

    private void addJavaLibrary(final RobotDryRunLibraryImport dryRunLibraryImport) {
        final JarStructureBuilder jarStructureBuilder = new JarStructureBuilder(project.getRuntimeEnvironment(),
                project.getRobotProjectConfig(), project.getProject());
        List<JarClass> classesFromJar = newArrayList();
        try {
            classesFromJar = jarStructureBuilder.provideEntriesFromFile(dryRunLibraryImport.getSourcePath());
        } catch (final RobotEnvironmentException e) {
            dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED, e.getMessage());
            return;
        }
        final Collection<ReferencedLibrary> librariesToAdd = new ArrayList<>();
        if (classesFromJar.isEmpty()) {
            dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED,
                    "RED was unable to find classes inside '" + dryRunLibraryImport.getSourcePath() + "' module.");
        } else {
            for (final JarClass jarClass : classesFromJar) {
                if (jarClass.getQualifiedName().equalsIgnoreCase(dryRunLibraryImport.getName())) {
                    librariesToAdd.add(jarClass.toReferencedLibrary(dryRunLibraryImport.getSourcePath()));
                }
            }
            if (librariesToAdd.isEmpty()) {
                dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.NOT_ADDED,
                        "RED was unable to find class '" + dryRunLibraryImport.getName() + "' inside '"
                                + dryRunLibraryImport.getSourcePath() + "' module.");
            }
        }

        addReferencedLibraries(dryRunLibraryImport, librariesToAdd);
    }

    private void addReferencedLibraries(final RobotDryRunLibraryImport dryRunLibraryImport,
            final Collection<ReferencedLibrary> librariesToAdd) {
        for (final ReferencedLibrary library : librariesToAdd) {
            if (config.addReferencedLibrary(library)) {
                addedLibs.add(library);
                dryRunLibraryImport.setStatusAndAdditionalInfo(DryRunLibraryImportStatus.ADDED, "");
            }
        }
    }

}
