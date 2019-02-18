/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.rf.ide.core.project.ImportPath;
import org.rf.ide.core.testdata.model.RobotExpressions;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.FileHyperlink;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProjectPathsProvider;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

abstract class HyperlinksToFilesDetector {

    protected final List<IHyperlink> detectHyperlinks(final RobotSuiteFile suiteFile, final IRegion fromRegion,
            final String pathAsString, final boolean isLibraryImport) {
        final List<IHyperlink> hyperlinks = new ArrayList<>();

        final String normalizedPath = RobotExpressions.unescapeSpaces(pathAsString);
        if (isLibraryImport && !isPath(normalizedPath)) {
            return hyperlinks;
        }

        final Optional<URI> absoluteUri = new RobotProjectPathsProvider(suiteFile.getRobotProject())
                .tryToFindAbsoluteUri(suiteFile.getFile(), ImportPath.from(normalizedPath));
        if (!absoluteUri.isPresent()) {
            return hyperlinks;
        }

        final Optional<IHyperlink> hyperlink = createLink(suiteFile, fromRegion, absoluteUri.get());
        if (hyperlink.isPresent()) {
            hyperlinks.add(hyperlink.get());
        }
        return hyperlinks;
    }

    private Optional<IHyperlink> createLink(final RobotSuiteFile suiteFile, final IRegion fromRegion, final URI path) {
        final IWorkspaceRoot wsRoot = suiteFile.getFile().getWorkspace().getRoot();
        final IResource resource = new RedWorkspace(wsRoot).forUri(path);
        return resource != null ? createHyperlink(fromRegion, resource) : Optional.empty();
    }

    private Optional<IHyperlink> createHyperlink(final IRegion fromRegion, final IResource destination) {
        if (destination != null && destination.exists() && destination.getType() == IResource.FILE) {
            return Optional.of(new FileHyperlink(fromRegion, (IFile) destination, "Open File", performAfterOpening()));
        }
        return Optional.empty();
    }

    protected abstract Consumer<IFile> performAfterOpening();

    private boolean isPath(final String pathAsString) {
        return pathAsString.endsWith("/") || pathAsString.endsWith(".py") || pathAsString.endsWith(".class")
                || pathAsString.endsWith(".java");
    }
}
