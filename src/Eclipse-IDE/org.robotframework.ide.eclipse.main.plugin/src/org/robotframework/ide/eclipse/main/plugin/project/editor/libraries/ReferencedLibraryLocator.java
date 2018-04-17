/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.core.runtime.Path;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.project.ImportPath;
import org.rf.ide.core.project.ImportSearchPaths;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.project.ResolvedImportPath;
import org.rf.ide.core.project.ResolvedImportPath.MalformedPathImportException;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;

public class ReferencedLibraryLocator {

    private final RobotProject robotProject;

    private final IReferencedLibraryImporter importer;

    private final IReferencedLibraryDetector detector;

    private final RobotProjectConfig projectConfig;

    private final Map<File, Collection<ReferencedLibrary>> libImportCache = new HashMap<>();

    public ReferencedLibraryLocator(final RobotProject robotProject, final IReferencedLibraryImporter importer,
            final IReferencedLibraryDetector detector) {
        this.robotProject = robotProject;
        this.importer = importer;
        this.detector = detector;
        this.projectConfig = robotProject.getRobotProjectConfig();
    }

    public void locateByName(final RobotSuiteFile suiteFile, final String name) {
        Optional<File> libraryFile = Optional.empty();
        try {
            libraryFile = findLibraryFileByName(suiteFile, name);
            if (libraryFile.isPresent()) {
                final SimpleImmutableEntry<File, Collection<ReferencedLibrary>> imported = importJavaOrPythonLibrary(
                        libraryFile.get());
                detector.libraryDetectedByName(name, imported.getKey(), imported.getValue());
            } else {
                detector.libraryDetectingByNameFailed(name, libraryFile, "Unable to find '" + name + "' library.");
            }
        } catch (final RobotEnvironmentException e) {
            detector.libraryDetectingByNameFailed(name, libraryFile, e.getMessage());
        }
    }

    private Optional<File> findLibraryFileByName(final RobotSuiteFile suiteFile, final String name) {
        final String currentFileDirectoryPath = suiteFile.getFile().getParent().getLocation().toOSString();
        final EnvironmentSearchPaths searchPaths = new RedEclipseProjectConfig(projectConfig)
                .createEnvironmentSearchPaths(robotProject.getProject());
        searchPaths.addPythonPath(currentFileDirectoryPath);
        searchPaths.addClassPath(currentFileDirectoryPath);
        return robotProject.getRuntimeEnvironment().getModulePath(name, searchPaths);
    }

    public void locateByPath(final RobotSuiteFile suiteFile, final String path) {
        Optional<File> libraryFile = Optional.empty();
        try {
            if (path.endsWith("/") || path.endsWith(".py")) {
                libraryFile = findLibraryFileByPath(suiteFile, path);
                if (libraryFile.isPresent()) {
                    final SimpleImmutableEntry<File, Collection<ReferencedLibrary>> imported = importPythonLibrary(
                            libraryFile.get());
                    detector.libraryDetectedByPath(path, imported.getKey(), imported.getValue());
                } else {
                    detector.libraryDetectingByPathFailed(path, libraryFile,
                            "Unable to find library under '" + path + "' location.");
                }
            } else {
                detector.libraryDetectingByPathFailed(path, libraryFile,
                        "The path '" + path + "' should point to either .py file or python module directory.");
            }
        } catch (final RobotEnvironmentException | MalformedPathImportException e) {
            detector.libraryDetectingByPathFailed(path, libraryFile, e.getMessage());
        }
    }

    private Optional<File> findLibraryFileByPath(final RobotSuiteFile suiteFile, final String path) {
        final Map<String, String> vars = suiteFile.getProject().getRobotProjectHolder().getVariableMappings();
        return ResolvedImportPath.from(ImportPath.from(path), vars).flatMap(resolvedPath -> {
            final PathsProvider pathsProvider = suiteFile.getProject().createPathsProvider();
            return new ImportSearchPaths(pathsProvider).findAbsoluteUri(suiteFile.getFile().getLocationURI(),
                    resolvedPath);
        }).map(File::new);
    }

    private SimpleImmutableEntry<File, Collection<ReferencedLibrary>> importJavaOrPythonLibrary(
            final File libraryFile) {
        if (libraryFile.getAbsolutePath().endsWith(".jar")) {
            return importLibsFromFileWithCaching(libraryFile,
                    () -> importer.importJavaLib(robotProject.getRuntimeEnvironment(), robotProject.getProject(),
                            projectConfig, libraryFile.getAbsolutePath()));
        } else {
            return importPythonLibrary(libraryFile);
        }
    }

    private SimpleImmutableEntry<File, Collection<ReferencedLibrary>> importPythonLibrary(final File libraryFile) {
        if (libraryFile.isDirectory() && new File(libraryFile, "__init__.py").exists()) {
            return importLibsFromFileWithCaching(new File(libraryFile, "__init__.py"),
                    () -> newArrayList(ReferencedLibrary.create(LibraryType.PYTHON, libraryFile.getName(),
                            new Path(libraryFile.getPath()).removeLastSegments(1).toPortableString())));
        } else {
            return importLibsFromFileWithCaching(libraryFile,
                    () -> importer.importPythonLib(robotProject.getRuntimeEnvironment(), robotProject.getProject(),
                            projectConfig, libraryFile.getAbsolutePath()));
        }
    }

    private SimpleImmutableEntry<File, Collection<ReferencedLibrary>> importLibsFromFileWithCaching(
            final File libraryFile, final Supplier<Collection<ReferencedLibrary>> importLibrarySupplier) {
        if (!libImportCache.containsKey(libraryFile)) {
            libImportCache.put(libraryFile, importLibrarySupplier.get());
        }
        return new SimpleImmutableEntry<>(libraryFile, libImportCache.get(libraryFile));
    }

    public interface IReferencedLibraryDetector {

        void libraryDetectedByName(String name, File libraryFile, Collection<ReferencedLibrary> referenceLibraries);

        void libraryDetectedByPath(String path, File libraryFile, Collection<ReferencedLibrary> referenceLibraries);

        void libraryDetectingByNameFailed(String name, Optional<File> libraryFile, String failReason);

        void libraryDetectingByPathFailed(String path, Optional<File> libraryFile, String failReason);
    }
}
