/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.rf.ide.core.project.ImportPath;
import org.rf.ide.core.project.ImportSearchPaths;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.project.ResolvedImportPath;
import org.rf.ide.core.project.ResolvedImportPath.MalformedPathImportException;
import org.rf.ide.core.testdata.model.RobotExpressions;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.FileHyperlink;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Optional;

abstract class HyperlinksToFilesDetector {

    protected final List<IHyperlink> detectHyperlinks(final RobotSuiteFile suiteFile, final IRegion fromRegion,
            final String pathAsString, final boolean isLibraryImport) {
        final List<IHyperlink> hyperlinks = new ArrayList<>();

        final String normalizedPath = RobotExpressions.unescapeSpaces(pathAsString);
        if (isLibraryImport && !isPath(normalizedPath)) {
            return hyperlinks;
        }

        final Optional<URI> absoluteUri = createAbsoluteUri(suiteFile, normalizedPath);
        if (!absoluteUri.isPresent()) {
            return hyperlinks;
        }

        final Optional<IHyperlink> hyperlink = createLink(suiteFile, fromRegion, absoluteUri.get());
        if (hyperlink.isPresent()) {
            hyperlinks.add(hyperlink.get());
        }
        return hyperlinks;
    }

    private Optional<URI> createAbsoluteUri(final RobotSuiteFile suiteFile, final String path) {
        final Map<String, String> variablesMapping = suiteFile.getProject()
                .getRobotProjectHolder()
                .getVariableMappings();
        try {
            final Optional<ResolvedImportPath> resolvedPath = ResolvedImportPath.from(ImportPath.from(path),
                    variablesMapping);
            if (!resolvedPath.isPresent()) {
                return Optional.absent();
            }
            final PathsProvider pathsProvider = suiteFile.getProject().createPathsProvider();
            final ImportSearchPaths searchPaths = new ImportSearchPaths(pathsProvider);
            return searchPaths.findAbsoluteUri(suiteFile.getFile().getLocationURI(), resolvedPath.get());
        } catch (final MalformedPathImportException e) {
            return Optional.absent();
        }
    }

    private Optional<IHyperlink> createLink(final RobotSuiteFile suiteFile, final IRegion fromRegion, final URI path) {
        final IWorkspaceRoot wsRoot = suiteFile.getFile().getWorkspace().getRoot();
        final IResource resource = new RedWorkspace(wsRoot).forUri(path);
        return resource != null ? createHyperlink(fromRegion, resource) : Optional.<IHyperlink> absent();
    }

    private Optional<IHyperlink> createHyperlink(final IRegion fromRegion, final IResource destination) {
        if (destination != null && destination.exists() && destination.getType() == IResource.FILE) {
            return Optional.<IHyperlink> of(new FileHyperlink(fromRegion, (IFile) destination, "Open File"));
        }
        return Optional.absent();
    }

    private boolean isPath(final String pathAsString) {
        return pathAsString.endsWith("/") || pathAsString.endsWith(".py") || pathAsString.endsWith(".class")
                || pathAsString.endsWith(".java");
    }
}
