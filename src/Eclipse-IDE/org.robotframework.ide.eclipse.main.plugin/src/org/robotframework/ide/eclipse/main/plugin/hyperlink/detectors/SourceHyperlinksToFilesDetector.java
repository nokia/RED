/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 */
public class SourceHyperlinksToFilesDetector extends HyperlinksToFilesDetector implements IHyperlinkDetector {

    private final RobotSuiteFile suiteFile;
    private String lineContent;

    public SourceHyperlinksToFilesDetector(final RobotSuiteFile suiteFile) {
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
            lineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document,
                    hyperlinkRegion.get().getOffset());
            if (!isApplicable(lineContent)) {
                return null;
            }
            final IRegion fromRegion = hyperlinkRegion.get();
            final String pathAsString = document.get(fromRegion.getOffset(), fromRegion.getLength());

            final List<IHyperlink> hyperlinks = detectHyperlinks(suiteFile, fromRegion, pathAsString);
            return hyperlinks.isEmpty() ? null : hyperlinks.toArray(new IHyperlink[0]);
        } catch (final BadLocationException e) {
            return null;
        }
    }

    private boolean isApplicable(final String lineContent) {
        return lineContent.trim().toLowerCase().startsWith("resource")
                || lineContent.trim().toLowerCase().startsWith("variables")
                || lineContent.trim().toLowerCase().startsWith("library");
    }

    @Override
    protected boolean isLibraryImport() {
        return lineContent.trim().toLowerCase().startsWith("library");
    }
}
