/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

public class KeywordHyperlinkDetector implements IHyperlinkDetector {

    private final TextEditorHoverManager hoverManager = new TextEditorHoverManager();

    @Override
    public IHyperlink[] detectHyperlinks(final ITextViewer viewer, final IRegion region, final boolean canShowMultipleHyperlinks) {

        final IRegion hyperlinkRegion = hoverManager.findHoveredText(viewer.getDocument(), region.getOffset());

        IRegion resultRegion = null;
        try {
            final String hoveredText = viewer.getDocument().get(hyperlinkRegion.getOffset(), hyperlinkRegion.getLength());
            final FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(viewer.getDocument());

            final int varBegin = hoveredText.indexOf("{");
            final int varEnd = hoveredText.indexOf("}");
            if (varBegin >= 0 && varEnd >= 0) {
                final String firstChar = String.valueOf(hoveredText.charAt(0));
                final String varName = hoveredText.substring(varBegin + 1, varEnd);
                resultRegion = adapter.find(0, "^([" + firstChar + "][{]" + varName + "[}])", true, true, false, true);
            } else {
                resultRegion = adapter.find(0, "^\\b" + hoveredText + "\\b", true, true, false, true);
            }
        } catch (final BadLocationException e) {
            e.printStackTrace();
        }

        if (resultRegion != null) {
            return new IHyperlink[] { new KeywordHyperlink(viewer, hyperlinkRegion, resultRegion) };
        }

        return null;
    }

}
