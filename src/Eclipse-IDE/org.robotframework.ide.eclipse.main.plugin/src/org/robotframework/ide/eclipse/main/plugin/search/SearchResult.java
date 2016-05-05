/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;


/**
 * @author Michal Anglart
 *
 */
public class SearchResult extends AbstractTextSearchResult {

    private final SearchQuery query;

    public SearchResult(final ISearchQuery query) {
        this.query = (SearchQuery) query;
    }

    @Override
    public String getLabel() {
        return query.getSettings().getSearchPattern().getPattern() + " - " + getMatchCount() + " matches";
    }

    @Override
    public String getTooltip() {
        return "tooltip";
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override
    public ISearchQuery getQuery() {
        return query;
    }

    @Override
    public IEditorMatchAdapter getEditorMatchAdapter() {
        return null;
    }

    @Override
    public IFileMatchAdapter getFileMatchAdapter() {
        return null;
    }

    @Override
    public void addMatch(final Match match) {
        super.addMatch(match);
    }

    boolean containMatches(final Object element) {
        return getMatches(element).length > 0;
    }
}
