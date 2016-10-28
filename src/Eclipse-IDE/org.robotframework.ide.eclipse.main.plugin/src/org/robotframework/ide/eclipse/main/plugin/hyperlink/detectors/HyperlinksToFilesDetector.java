/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.FileHyperlink;
import org.robotframework.ide.eclipse.main.plugin.model.ImportSearchPaths;
import org.robotframework.ide.eclipse.main.plugin.model.ImportSearchPaths.MarkedPath;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Optional;

abstract class HyperlinksToFilesDetector {

    protected final List<IHyperlink> detectHyperlinks(final RobotSuiteFile suiteFile, final IRegion fromRegion,
            final String pathAsString, final boolean isLibraryImport) {
        final List<IHyperlink> hyperlinks = new ArrayList<>();

        final String normalizedPath = pathAsString.replaceAll(" [\\\\] ", " ");
        if (isLibraryImport && !isPath(normalizedPath)) {
            return hyperlinks;
        }

        final IPath absolutePath = createAbsolutePath(suiteFile, pathAsString);
        if (absolutePath == null) {
            return hyperlinks;
        }

        final Optional<IHyperlink> hyperlink = createLink(suiteFile, fromRegion, absolutePath);
        if (hyperlink.isPresent()) {
            hyperlinks.add(hyperlink.get());
        }
        return hyperlinks;
    }

    private IPath createAbsolutePath(final RobotSuiteFile suiteFile, final String pathAsString) {
        final String resolved = suiteFile.getProject().resolve(pathAsString);
        final IPath path = new Path(resolved);
        if (path.isAbsolute()) {
            return path;
        } else {
            final ImportSearchPaths searchPaths = new ImportSearchPaths(suiteFile.getProject());
            final Optional<MarkedPath> markedPath = searchPaths.getAbsolutePath(suiteFile, path);
            return markedPath.isPresent() ? markedPath.get().getPath() : null;
        }
    }

    private Optional<IHyperlink> createLink(final RobotSuiteFile suiteFile, final IRegion fromRegion,
            final IPath absolutePath) {
        final IWorkspaceRoot wsRoot = suiteFile.getFile().getWorkspace().getRoot();
        // FIXME : linked folders!
        final IPath wsRelativePath = absolutePath.makeRelativeTo(wsRoot.getLocation());
        if (wsRoot.getLocation().isPrefixOf(absolutePath)
                || wsRoot.findFilesForLocationURI(absolutePath.toFile().toURI()).length > 0) {
            // points into the workspace
            IResource resource = wsRoot.findMember(wsRelativePath);
            if (resource == null) {
                resource = wsRoot.findFilesForLocationURI(absolutePath.toFile().toURI())[0];
            }
            if (resource != null) {
                return createHyperlink(fromRegion, resource);
            }
        }
        return Optional.absent();
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
