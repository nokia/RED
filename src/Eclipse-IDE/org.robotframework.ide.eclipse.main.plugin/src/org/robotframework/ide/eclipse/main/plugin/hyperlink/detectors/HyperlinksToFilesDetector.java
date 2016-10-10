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
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.FileHyperlink;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

abstract class HyperlinksToFilesDetector {

    protected final List<IHyperlink> detectHyperlinks(final RobotSuiteFile suiteFile, final IRegion fromRegion,
            final String pathAsString) {
        final List<IHyperlink> hyperlinks = new ArrayList<>();

        final String normalizedPath = pathAsString.replaceAll(" [\\\\] ", " ");
        if (isLibraryImport() && !isPath(normalizedPath)) {
            return hyperlinks;
        }

        final Path path = new Path(suiteFile.getProject().resolve(pathAsString));
        final IWorkspaceRoot wsRoot = suiteFile.getFile().getWorkspace().getRoot();
        IPath wsRelativePath = null;
        if (path.isAbsolute()) {
            wsRelativePath = path.makeRelativeTo(wsRoot.getLocation());
            if (!wsRoot.getLocation().isPrefixOf(path)) {
                return hyperlinks;
            }
        }
        if (wsRelativePath == null) {
            wsRelativePath = PathsConverter.fromResourceRelativeToWorkspaceRelative(suiteFile.getFile(), path);
        }
        final IResource resource = wsRoot.findMember(wsRelativePath);
        if (resource != null && resource.exists() && resource.getType() == IResource.FILE) {
            hyperlinks.add(new FileHyperlink(fromRegion, (IFile) resource, "Open File"));
        }
        return hyperlinks;
    }

    protected abstract boolean isLibraryImport();

    private boolean isPath(final String pathAsString) {
        return pathAsString.endsWith("/") || pathAsString.endsWith(".py") || pathAsString.endsWith(".class")
                || pathAsString.endsWith(".java");
    }
}
