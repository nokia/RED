/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.search.participants.DocumentationSearch;
import org.robotframework.ide.eclipse.main.plugin.search.participants.KeywordSearch;
import org.robotframework.ide.eclipse.main.plugin.search.participants.TargetedSearch;
import org.robotframework.ide.eclipse.main.plugin.search.participants.TestCaseSearch;
import org.robotframework.ide.eclipse.main.plugin.search.participants.VariableSearch;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 *
 */
public class SearchQuery implements ISearchQuery {

    private final SearchSettings settings;

    private final SearchResult result;

    private final RobotModel model;

    public SearchQuery(final SearchSettings settings) {
        this(settings, RedPlugin.getModelManager().getModel());
    }

    @VisibleForTesting
    SearchQuery(final SearchSettings settings, final RobotModel model) {
        this.settings = settings;
        this.result = new SearchResult(this);
        this.model = model;
    }

    SearchSettings getSettings() {
        return settings;
    }

    @Override
    public IStatus run(final IProgressMonitor monitor) throws OperationCanceledException {
        monitor.setTaskName("Searching for '" + settings.getSearchPattern().getPattern() + "'");
        
        monitor.subTask("Collecting search targets");
        final SearchQueryTargets queryTargets = new SearchQueryTargets(model);
        queryTargets.collect(settings.getResourcesRoots(), settings.getTargets());

        if (monitor.isCanceled()) {
            return new Status(IStatus.CANCEL, RedPlugin.PLUGIN_ID, "Search has been cancelled");
        }

        try {
            final TargetedSearch search = getSearcher();
            search.run(monitor, queryTargets.getLibrariesToSearch(), queryTargets.getResourcesToSearch());
            return new Status(IStatus.OK, RedPlugin.PLUGIN_ID, "Search ended");
        } catch (final OperationCanceledException e) {
            return new Status(IStatus.CANCEL, RedPlugin.PLUGIN_ID, "Search has been cancelled");
        }
    }

    private TargetedSearch getSearcher() {
        switch (settings.getSearchFor()) {
            case KEYWORD:
                return new KeywordSearch(settings.getSearchPattern(), model, result);
            case TEST_CASE:
                return new TestCaseSearch(settings.getSearchPattern(), model, result);
            case VARIABLE:
                return new VariableSearch(settings.getSearchPattern(), model, result);
            case DOC_CONTENT:
                return new DocumentationSearch(settings.getSearchPattern(), model, result);
            default:
                throw new IllegalStateException("Unrecognized search: " + settings.getSearchFor());
        }
    }

    @Override
    public String getLabel() {
        return "Robot Search";
    }

    @Override
    public boolean canRerun() {
        // TODO : this should be implemented properly; currently changing to true and running search
        // again will cause multiplying same results
        return false;
    }

    @Override
    public boolean canRunInBackground() {
        return true;
    }

    @Override
    public ISearchResult getSearchResult() {
        return result;
    }

    public RobotModel getModel() {
        return model;
    }
}
