/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.project.ImportPath;
import org.rf.ide.core.project.ImportSearchPaths;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.project.ResolvedImportPath;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;

public class ReferencedLibraryFinder {

    private final RobotSuiteFile suiteFile;

    private final ReferencedLibraryImporter importer;

    public ReferencedLibraryFinder(final RobotSuiteFile suiteFile, final ReferencedLibraryImporter importer) {
        this.suiteFile = suiteFile;
        this.importer = importer;
    }

    public Collection<ReferencedLibrary> findByName(final RobotProjectConfig config, final String name)
            throws IncorrectLibraryPathException, UnknownLibraryException {
        try {
            final RobotProject robotProject = suiteFile.getProject();
            final File libraryFile = findLibraryFile(name, robotProject, config)
                    .orElseThrow(() -> new UnknownLibraryException("Unable to find '" + name + "' library."));
            if (libraryFile.getAbsolutePath().endsWith(".jar")) {
                final IPath resolvedAbsPath = new Path(libraryFile.getAbsolutePath());
                return importer.importJavaLib(robotProject.getRuntimeEnvironment(), robotProject.getProject(), config,
                        resolvedAbsPath.toString());
            } else {
                if (isPythonModule(libraryFile)) {
                    return newArrayList(ReferencedLibrary.create(LibraryType.PYTHON, name,
                            new Path(libraryFile.getPath()).toPortableString()));
                } else {
                    return findByPath(config, libraryFile.getAbsolutePath());
                }
            }
        } catch (final RobotEnvironmentException e) {
            throw new UnknownLibraryException(e);
        }
    }

    private Optional<File> findLibraryFile(final String name, final RobotProject robotProject,
            final RobotProjectConfig config) {
        final String currentFileDirectoryPath = suiteFile.getFile().getParent().getLocation().toOSString();
        final EnvironmentSearchPaths searchPaths = new RedEclipseProjectConfig(config)
                .createEnvironmentSearchPaths(robotProject.getProject());
        searchPaths.addPythonPath(currentFileDirectoryPath);
        searchPaths.addClassPath(currentFileDirectoryPath);
        return robotProject.getRuntimeEnvironment().getModulePath(name, searchPaths);
    }

    public Collection<ReferencedLibrary> findByPath(final RobotProjectConfig config, final String path)
            throws IncorrectLibraryPathException {
        if (path.endsWith("/") || path.endsWith(".py")) {
            final RobotProject robotProject = suiteFile.getProject();
            final File libraryFile = findLibraryFile(path).orElseThrow(
                    () -> new IncorrectLibraryPathException("Unable to find library under '" + path + "' location."));
            if (isPythonModule(libraryFile)) {
                return newArrayList(ReferencedLibrary.create(LibraryType.PYTHON, libraryFile.getName(),
                        new Path(libraryFile.getPath()).toPortableString()));
            } else {
                return importer.importPythonLib(robotProject.getRuntimeEnvironment(), robotProject.getProject(), config,
                        libraryFile.getAbsolutePath());
            }
        } else {
            throw new IncorrectLibraryPathException(
                    "The path '" + path + "' should point to either .py file or python module directory.");
        }
    }

    private Optional<File> findLibraryFile(final String path) {
        final Map<String, String> vars = suiteFile.getProject().getRobotProjectHolder().getVariableMappings();
        final ResolvedImportPath resolvedPath = ResolvedImportPath.from(ImportPath.from(path), vars).get();
        final PathsProvider pathsProvider = suiteFile.getProject().createPathsProvider();
        return new ImportSearchPaths(pathsProvider).findAbsoluteUri(suiteFile.getFile().getLocationURI(), resolvedPath)
                .map(File::new);
    }

    private static boolean isPythonModule(final File file) {
        return file.isDirectory() && new File(file, "__init__.py").exists();
    }

    @SuppressWarnings("serial")
    public static class UnknownLibraryException extends Exception {

        UnknownLibraryException(final String message) {
            super(message);
        }

        UnknownLibraryException(final Throwable cause) {
            super(cause);
        }

    }

    @SuppressWarnings("serial")
    public static class IncorrectLibraryPathException extends Exception {

        IncorrectLibraryPathException(final String message) {
            super(message);
        }

    }
}
