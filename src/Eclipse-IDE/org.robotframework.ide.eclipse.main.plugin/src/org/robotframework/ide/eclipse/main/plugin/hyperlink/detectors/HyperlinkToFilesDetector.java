/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.FileHyperlink;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 */
public class HyperlinkToFilesDetector implements IHyperlinkDetector {

    private final RobotSuiteFile suiteFile;

    public HyperlinkToFilesDetector(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    @Override
    public IHyperlink[] detectHyperlinks(final ITextViewer textViewer, final IRegion region,
            final boolean canShowMultipleHyperlinks) {
        try {
            final IDocument document = textViewer.getDocument();
            final Optional<IRegion> hyperlinkRegion = DocumentUtilities.findCellRegion(document, suiteFile.isTsvFile(),
                    region.getOffset());
            if (!hyperlinkRegion.isPresent()) {
                return null;
            }

            final String lineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document,
                    hyperlinkRegion.get().getOffset());
            if (!isApplicable(lineContent)) {
                return null;
            }
            final IRegion fromRegion = hyperlinkRegion.get();

            String pathAsString = document.get(fromRegion.getOffset(), fromRegion.getLength());
            pathAsString = pathAsString.replaceAll(" [\\\\] ", "  ");
            if (lineContent.trim().toLowerCase().startsWith("library") && !isPath(pathAsString)) {
                return null;
            }

            final Path path = new Path(suiteFile.getProject().resolve(pathAsString));
            final IWorkspaceRoot wsRoot = suiteFile.getFile().getWorkspace().getRoot();
            IPath wsRelativePath = null;
            if (path.isAbsolute()) {
                wsRelativePath = path.makeRelativeTo(wsRoot.getLocation());
                if (!wsRoot.getLocation().isPrefixOf(path)) {
                    return null;
                }
            }
            if (wsRelativePath == null) {
                wsRelativePath = PathsConverter.fromResourceRelativeToWorkspaceRelative(suiteFile.getFile(), path);
            }
            final IResource resource = wsRoot.findMember(wsRelativePath);
            if (resource != null && resource.exists() && resource.getType() == IResource.FILE) {

                final List<IHyperlink> hyperlinks = newArrayList();
                hyperlinks.add(new FileHyperlink(fromRegion, (IFile) resource, "Open File"));

                if (canShowMultipleHyperlinks && hyperlinks.size() > 1) {
                    throw new IllegalStateException(
                            "Cannot provide more than one hyperlink, but there were " + hyperlinks.size() + " found");
                }
                return hyperlinks.isEmpty() ? null : hyperlinks.toArray(new IHyperlink[0]);
            }
            return null;
        } catch (final BadLocationException e) {
            return null;
        }
    }

    private boolean isApplicable(final String lineContent) {
        return lineContent.trim().toLowerCase().startsWith("resource")
                || lineContent.trim().toLowerCase().startsWith("variables")
                || lineContent.trim().toLowerCase().startsWith("library");
    }

    private boolean isPath(final String pathAsString) {
        return pathAsString.endsWith("/") || pathAsString.endsWith(".py") || pathAsString.endsWith(".class")
                || pathAsString.endsWith(".java");
    }
}
