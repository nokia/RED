/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.hyperlinks;

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
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
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
            final Optional<IRegion> hyperlinkRegion = DocumentUtilities.findCellRegion(document,
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

            final IWorkspaceRoot wsRoot = suiteFile.getFile().getWorkspace().getRoot();
            final String pathAsString = document.get(fromRegion.getOffset(), fromRegion.getLength());
            final Path path = new Path(pathAsString);

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
                return new IHyperlink[] { new DifferentFileHyperlink(fromRegion, (IFile) resource) };
            }
            return null;
        } catch (final BadLocationException e) {
            return null;
        }
    }

    private boolean isApplicable(final String lineContent) {
        return lineContent.trim().toLowerCase().startsWith("resource")
                || lineContent.trim().toLowerCase().startsWith("variables");
    }

}
