/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Position;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;

public class MatchesInFileEngine implements MatchingEngine {

    private final IFile fileForSearching;

    public MatchesInFileEngine(final IFile fileForSearching) {
        this.fileForSearching = fileForSearching;
    }

    @Override
    public void searchForMatches(final String toMatch, final MatchAccess matchAccess) {
        final Pattern toMatchPattern = Pattern.compile(toMatch);

        final TextSearchEngine searchEngine = TextSearchEngine.create();
        searchEngine.search(new IFile[] { fileForSearching }, new TextSearchRequestor() {

            @Override
            public boolean acceptPatternMatch(final TextSearchMatchAccess searchMatchAccess) throws CoreException {
                final int offset = searchMatchAccess.getMatchOffset();
                final int length = searchMatchAccess.getMatchLength();
                
                final Position matchPosition = new Position(offset, length);
                matchAccess.onMatch(searchMatchAccess.getFileContent(offset, length), matchPosition);

                return true;
            }
        }, toMatchPattern, new NullProgressMonitor());
    }
}