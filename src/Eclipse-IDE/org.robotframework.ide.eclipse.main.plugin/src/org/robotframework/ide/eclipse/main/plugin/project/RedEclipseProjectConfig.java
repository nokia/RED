/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.EnvironmentVariableReplacer;
import org.rf.ide.core.SystemVariableAccessor;
import org.rf.ide.core.environment.EnvironmentSearchPaths;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RelativeTo;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace.Paths;

public class RedEclipseProjectConfig {

    private final IProject project;

    private final RobotProjectConfig config;

    private final SystemVariableAccessor variableAccessor;

    private final Supplier<EnvironmentVariableReplacer> variableReplacer;

    public RedEclipseProjectConfig(final IProject project, final RobotProjectConfig config) {
        this(project, config, new SystemVariableAccessor());
    }

    public RedEclipseProjectConfig(final IProject project, final RobotProjectConfig config,
            final SystemVariableAccessor variableAccessor) {
        this(project, config, variableAccessor, () -> new EnvironmentVariableReplacer(variableAccessor));
    }

    public RedEclipseProjectConfig(final IProject project, final RobotProjectConfig config,
            final SystemVariableAccessor variableAccessor,
            final Supplier<EnvironmentVariableReplacer> variableReplacer) {
        this.project = project;
        this.config = config;
        this.variableAccessor = variableAccessor;
        this.variableReplacer = variableReplacer;
    }

    public IPath resolveToAbsolutePath(final SearchPath path) {
        final IContainer base = config.getRelativityPoint().getRelativeTo() == RelativeTo.WORKSPACE
                ? project.getWorkspace().getRoot()
                : project;
        final IPath resolvedPath = new Path(
                variableReplacer.get().replaceKnownEnvironmentVariables(path.getLocation()));
        return Paths.toAbsoluteFromRelativeIfPossible(base, resolvedPath);
    }

    public IPath resolveToAbsolutePath(final ReferencedLibrary lib) {
        return Paths.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(lib.getPath()));
    }

    public IPath resolveToAbsolutePath(final String path) {
        return Paths.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(path));
    }

    public EnvironmentSearchPaths createAdditionalEnvironmentSearchPaths() {
        return new EnvironmentSearchPaths(resolvePaths(config.getClassPaths()), resolvePaths(config.getPythonPaths()));
    }

    public EnvironmentSearchPaths createExecutionEnvironmentSearchPaths() {
        final List<String> classPaths = new ArrayList<>();
        classPaths.add(".");
        classPaths.addAll(resolveLibPaths(LibraryType.JAVA));
        classPaths.addAll(resolvePaths(config.getClassPaths()));
        classPaths.addAll(variableAccessor.getPaths("CLASSPATH"));
        final List<String> pythonPaths = new ArrayList<>();
        pythonPaths.addAll(resolveLibPaths(LibraryType.PYTHON));
        pythonPaths.addAll(resolvePaths(config.getPythonPaths()));
        return new EnvironmentSearchPaths(classPaths, pythonPaths);
    }

    private List<String> resolvePaths(final List<SearchPath> paths) {
        return paths.stream()
                .map(this::resolveToAbsolutePath)
                .filter(Paths::isCorrect)
                .map(IPath::toOSString)
                .collect(toList());
    }

    private List<String> resolveLibPaths(final LibraryType libType) {
        return config.getReferencedLibraries()
                .stream()
                .filter(lib -> lib.provideType() == libType)
                .map(ReferencedLibrary::getParentPath)
                .map(this::resolveToAbsolutePath)
                .map(IPath::toOSString)
                .collect(toList());
    }
}
