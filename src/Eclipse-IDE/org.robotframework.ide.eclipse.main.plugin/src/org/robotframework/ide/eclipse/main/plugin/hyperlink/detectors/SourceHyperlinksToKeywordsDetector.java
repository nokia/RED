/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.RegionsHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.SuiteFileSourceRegionHyperlink;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;

import com.google.common.base.Optional;

/**
 * @author mmarzec
 */
public class SourceHyperlinksToKeywordsDetector extends HyperlinksToKeywordsDetector implements IHyperlinkDetector {

    private final RobotSuiteFile suiteFile;

    private ITextViewer textViewer;

    public SourceHyperlinksToKeywordsDetector(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    @Override
    public IHyperlink[] detectHyperlinks(final ITextViewer textViewer, final IRegion region,
            final boolean canShowMultipleHyperlinks) {
        this.textViewer = textViewer;

        try {
            final Optional<IRegion> hyperlinkRegion = DocumentUtilities.findCellRegion(textViewer.getDocument(),
                    suiteFile.isTsvFile(), region.getOffset());
            if (!hyperlinkRegion.isPresent()) {
                return null;
            }
            final IRegion fromRegion = hyperlinkRegion.get();
            final String keywordName = textViewer.getDocument().get(fromRegion.getOffset(), fromRegion.getLength());
            final List<IHyperlink> hyperlinks = detectHyperlinks(suiteFile, fromRegion, keywordName);

            return hyperlinks.isEmpty() ? null : hyperlinks.toArray(new IHyperlink[0]);

        } catch (final BadLocationException e) {
            return null;
        }
    }

    @Override
    protected IHyperlink createLocalKeywordHyperlink(final KeywordHyperlinkEntity keywordEntity, final IRegion from,
            final String additionalInfo) {
        return new RegionsHyperlink(textViewer, keywordEntity.exposingResource, from,
                keywordEntity.destinationPosition.toRegion(), additionalInfo);
    }

    @Override
    protected IHyperlink createResourceKeywordHyperlink(final KeywordHyperlinkEntity keywordEntity, final IRegion from,
            final String additionalInfo) {
        return new SuiteFileSourceRegionHyperlink(from, keywordEntity.exposingResource,
                keywordEntity.destinationPosition.toRegion(), additionalInfo);
    }
}
