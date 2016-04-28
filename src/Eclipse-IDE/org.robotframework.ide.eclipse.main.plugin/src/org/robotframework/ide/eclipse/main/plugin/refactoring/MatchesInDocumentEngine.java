/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

public class MatchesInDocumentEngine implements MatchingEngine {

    private final IDocument documentForSearching;

    public MatchesInDocumentEngine(final IDocument documentForSearching) {
        this.documentForSearching = documentForSearching;
    }

    @Override
    public void searchForMatches(final String toMatch, final MatchAccess matchAccess) {
        try {
            searchFor(toMatch, matchAccess);
        } catch (final BadLocationException e) {
            RedPlugin.logError("Unable to locate matches in red.xml document", e);
        }
    }

    private void searchFor(final String toMatch, final MatchAccess matchAccess) throws BadLocationException {
        final FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(documentForSearching);
        IRegion matchingRegion = finder.find(0, toMatch, true, true, false, true);
        while (matchingRegion != null) {
            final int offset = matchingRegion.getOffset();
            final int length = matchingRegion.getLength();

            final Position matchPosition = new Position(offset, length);
            matchAccess.onMatch(documentForSearching.get(offset, length), matchPosition);

            matchingRegion = finder.find(offset + length, toMatch, true, true, false, true);
        }
    }
}