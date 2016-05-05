/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.resources.IResource;

/**
 * @author Michal Anglart
 *
 */
public class SearchSettings {

    private final SearchPattern pattern = new SearchPattern("");

    private boolean isCaseSensitive = false;

    private SearchFor searchFor = SearchFor.KEYWORD;

    private SearchLimitation limitation = SearchLimitation.NO_LIMITS;

    private EnumSet<SearchTarget> targets = EnumSet.of(SearchTarget.SUITE, SearchTarget.RESOURCE,
            SearchTarget.STANDARD_LIBRARY, SearchTarget.REFERENCED_LIBRARY);

    private List<IResource> resourcesRoots = new ArrayList<>();

    public SearchPattern getSearchPattern() {
        return pattern;
    }

    public SearchFor getSearchFor() {
        return searchFor;
    }

    public void setSearchFor(final SearchFor searchFor) {
        this.searchFor = searchFor;
    }

    public SearchLimitation getSearchLimitation() {
        return limitation;
    }

    public void setSearchLimitation(final SearchLimitation limitation) {
        this.limitation = limitation;
    }

    public EnumSet<SearchTarget> getTargets() {
        return targets;
    }

    public void setTargets(final EnumSet<SearchTarget> targets) {
        this.targets = targets;
    }

    public void addTarget(final SearchTarget target) {
        this.targets.add(target);
    }

    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    public void setCaseSensitive(final boolean isCaseSesitive) {
        this.isCaseSensitive = isCaseSesitive;
    }

    public void setResourcesRoots(final List<IResource> resourcesRoots) {
        this.resourcesRoots = resourcesRoots;
    }

    public List<IResource> getResourcesRoots() {
        return resourcesRoots;
    }

    public enum SearchFor {
        KEYWORD("Keyword"),
        TEST_CASE("Test case"),
        VARIABLE("Variable"),
        DOC_CONTENT("Keyword/Library documentation content");

        private final String label;

        private SearchFor(final String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum SearchLimitation {
        NO_LIMITS("All occurences"),
        ONLY_REFERENCES("References"),
        ONLY_DECLARATIONS("Declarations");

        private final String label;

        private SearchLimitation(final String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
    
    public enum SearchTarget {
        SUITE("Suite files"),
        RESOURCE("Resource files"),
        STANDARD_LIBRARY("Standard libraries"),
        REFERENCED_LIBRARY("Referenced libraries"),
        VARIABLE_FILE("Variable files");
        
        private final String label;

        private SearchTarget(final String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
