/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.EnvironmentVariableReplacer;
import org.rf.ide.core.project.ImportPath;
import org.rf.ide.core.project.ImportSearchPaths;
import org.rf.ide.core.project.ImportSearchPaths.MarkedUri;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.project.ResolvedImportPath;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;

public class RobotProjectPathsProvider {

    private final RobotProject robotProject;

    public RobotProjectPathsProvider(final RobotProject robotProject) {
        this.robotProject = robotProject;
    }

    public Optional<URI> findAbsoluteUri(final IFile file, final ImportPath importPath) throws URISyntaxException {
        final Map<String, String> variablesMapping = robotProject.getRobotProjectHolder().getVariableMappings();
        return ResolvedImportPath.from(importPath, variablesMapping).flatMap(resolvedPath -> {
            if (importPath.isAbsolute()) {
                return Optional.of(resolvedPath.getUri());
            }
            final ImportSearchPaths searchPaths = new ImportSearchPaths(createPathsProvider());
            return searchPaths.findAbsoluteUri(RedWorkspace.tryToGetLocalUri(file), resolvedPath);
        });
    }

    public Optional<MarkedUri> findAbsoluteMarkedUri(final IFile file, final ImportPath importPath)
            throws URISyntaxException {
        final Map<String, String> variablesMapping = robotProject.getRobotProjectHolder().getVariableMappings();
        return ResolvedImportPath.from(importPath, variablesMapping).flatMap(resolvedPath -> {
            final ImportSearchPaths searchPaths = new ImportSearchPaths(createPathsProvider());
            return searchPaths.findAbsoluteMarkedUri(RedWorkspace.tryToGetLocalUri(file), resolvedPath);
        });
    }

    public Optional<URI> tryToFindAbsoluteUri(final IFile file, final ImportPath importPath) {
        try {
            return findAbsoluteUri(file, importPath);
        } catch (final URISyntaxException e) {
            return Optional.empty();
        }
    }

    public Optional<MarkedUri> tryToFindAbsoluteMarkedUri(final IFile file, final ImportPath importPath) {
        try {
            return findAbsoluteMarkedUri(file, importPath);
        } catch (final URISyntaxException e) {
            return Optional.empty();
        }
    }

    PathsProvider createPathsProvider() {
        return new PathsProvider() {

            @Override
            public boolean targetExists(final URI uri) {
                if (uri.getScheme().equalsIgnoreCase("file")) {
                    try {
                        return new File(uri).exists();
                    } catch (final IllegalArgumentException e) {
                        return false;
                    }
                } else {
                    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                    return Stream
                            .concat(Stream.of(root.findFilesForLocationURI(uri)),
                                    Stream.of(root.findContainersForLocationURI(uri)))
                            .filter(IResource::exists)
                            .findFirst()
                            .isPresent();
                }
            }

            @Override
            public List<File> providePythonModulesSearchPaths() {
                return robotProject.getRobotProjectHolder().getModuleSearchPaths();
            }

            @Override
            public List<File> provideUserSearchPaths() {
                final RobotProjectConfig configuration = robotProject.getRobotProjectConfig();
                final EnvironmentVariableReplacer variableReplacer = new EnvironmentVariableReplacer();
                final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(robotProject.getProject(),
                        configuration);
                return configuration.getPythonPaths()
                        .stream()
                        .map(SearchPath::getLocation)
                        .map(variableReplacer::replaceKnownEnvironmentVariables)
                        .map(Path::new)
                        .map(redConfig::toAbsolutePath)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(toList());
            }
        };
    }

}
