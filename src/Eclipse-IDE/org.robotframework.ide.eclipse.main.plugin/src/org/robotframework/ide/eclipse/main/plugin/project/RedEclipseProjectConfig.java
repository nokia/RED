/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.RedURI;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.RelativeTo;
import org.rf.ide.core.project.RobotProjectConfig.RelativityPoint;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace.Paths;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.escape.Escaper;

public class RedEclipseProjectConfig {

    private final RobotProjectConfig config;

    public RedEclipseProjectConfig(final RobotProjectConfig config) {
        this.config = config;
    }

    public Optional<File> toAbsolutePath(final SearchPath path, final IProject containingProject) {
        final IPath asPath = new Path(path.getLocation());
        if (asPath.isAbsolute()) {
            return Optional.of(asPath.toFile());
        }
        final Optional<File> wsAbsolute = resolveToAbsolutePath(
                getRelativityLocation(config.getRelativityPoint(), containingProject), asPath).map(IPath::toFile);
        return wsAbsolute.map(file -> {
            final IPath wsRelative = Paths.toWorkspaceRelativeIfPossible(new Path(file.getAbsolutePath()));
            final IResource member = containingProject.getWorkspace().getRoot().findMember(wsRelative);
            if (member == null) {
                return file;
            } else {
                return member.getLocation().toFile();
            }
        });
    }

    @VisibleForTesting
    static Optional<IPath> resolveToAbsolutePath(final IPath base, final IPath child) {
        if (child.isAbsolute()) {
            return Optional.of(child);
        } else {
            try {
                final Escaper escaper = RedURI.URI_SPECIAL_CHARS_ESCAPER;

                final String portablePath = base.toPortableString();
                final URI filePath = new URI(escaper.escape(portablePath));
                final URI pathUri = filePath.resolve(escaper.escape(child.toString()));
                return Optional.of(new Path(RedURI.reverseUriSpecialCharsEscapes(pathUri.toString())));
            } catch (final Exception e) {
                return Optional.empty();
            }
        }
    }

    private static IPath getRelativityLocation(final RelativityPoint relativityPoint,
            final IProject containingProject) {
        final IPath result = relativityPoint.getRelativeTo() == RelativeTo.WORKSPACE
                ? containingProject.getWorkspace().getRoot().getLocation()
                : containingProject.getLocation();
        return result.addTrailingSeparator();
    }

    public EnvironmentSearchPaths createAdditionalEnvironmentSearchPaths(final IProject project) {
        return new EnvironmentSearchPaths(getResolvedPaths(project, config.getClassPath()),
                getResolvedPaths(project, config.getPythonPath()));
    }

    public EnvironmentSearchPaths createExecutionEnvironmentSearchPaths(final IProject project) {
        final List<String> classPaths = new ArrayList<>();
        classPaths.add(".");
        classPaths.addAll(getReferenceLibPaths(LibraryType.JAVA));
        classPaths.addAll(getResolvedPaths(project, config.getClassPath()));
        final List<String> pythonPaths = new ArrayList<>();
        pythonPaths.addAll(getReferenceLibPaths(LibraryType.PYTHON));
        pythonPaths.addAll(getResolvedPaths(project, config.getPythonPath()));
        return new EnvironmentSearchPaths(classPaths, pythonPaths);
    }

    private List<String> getResolvedPaths(final IProject containingProject, final List<SearchPath> paths) {
        return paths.stream()
                .map(path -> toAbsolutePath(path, containingProject))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(File::getPath)
                .collect(toList());
    }

    private List<String> getReferenceLibPaths(final LibraryType libType) {
        return config.getLibraries()
                .stream()
                .filter(lib -> lib.provideType() == libType)
                .map(lib -> Paths.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(lib.getPath())).toOSString())
                .collect(toList());
    }

    public List<String> getVariableFilePaths() {
        return config.getReferencedVariableFiles().stream().map(file -> {
            final String path = Paths.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(file.getPath())).toOSString();
            final List<String> args = file.getArguments();
            final String arguments = args.isEmpty() ? "" : ":" + String.join(":", args);
            return path + arguments;
        }).collect(toList());
    }
}
