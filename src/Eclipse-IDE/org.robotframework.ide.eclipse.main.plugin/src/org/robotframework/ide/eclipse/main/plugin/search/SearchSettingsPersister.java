/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import static com.google.common.collect.Lists.newArrayList;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.search.SearchSettings.SearchFor;
import org.robotframework.ide.eclipse.main.plugin.search.SearchSettings.SearchLimitation;
import org.robotframework.ide.eclipse.main.plugin.search.SearchSettings.SearchTarget;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 */
class SearchSettingsPersister {

    @VisibleForTesting
    static final int MAXIMUM_NUMBER_OF_RECENT_SEARCHES = 20;

    @VisibleForTesting
    static final String SEARCH_SETTINGS_SECTION_ID = RedSearchPage.ID;

    @VisibleForTesting
    static final String SEARCH_FOR_ATTRIBUTE = "searchFor";

    @VisibleForTesting
    static final String SEARCH_LIMIT_ATTRIBUTE = "searchLimit";

    @VisibleForTesting
    static final String SEARCH_TARGET_ATTRIBUTE = "searchTarget";

    @VisibleForTesting
    static final String CASE_SENSITIVE_ATTRIBUTE = "caseSensitive";

    @VisibleForTesting
    static final String PATTERNS_ATTRIBUTE = "patterns";

    private final IDialogSettings dialogSettings;

    SearchSettingsPersister() {
        this(RedPlugin.getDefault().getDialogSettings());
    }

    @VisibleForTesting
    SearchSettingsPersister(final IDialogSettings dialogSettings) {
        this.dialogSettings = dialogSettings;
    }

    void writeSettings(final SearchSettings settings) {
        IDialogSettings searchPageSection = dialogSettings.getSection(SEARCH_SETTINGS_SECTION_ID);
        if (searchPageSection == null) {
            searchPageSection = dialogSettings.addNewSection(SEARCH_SETTINGS_SECTION_ID);
        }
        searchPageSection.put(SEARCH_FOR_ATTRIBUTE, settings.getSearchFor().name());
        searchPageSection.put(SEARCH_LIMIT_ATTRIBUTE, settings.getSearchLimitation().name());
        searchPageSection.put(SEARCH_TARGET_ATTRIBUTE, getTargets(settings));
        searchPageSection.put(CASE_SENSITIVE_ATTRIBUTE, settings.isCaseSensitive());
        addRecentPattern(searchPageSection, settings.getSearchPattern().getPattern());
    }

    private void addRecentPattern(final IDialogSettings searchPageSection, final String pattern) {
        final String[] currentPatterns = searchPageSection.getArray(PATTERNS_ATTRIBUTE);

        final List<String> newPatterns = newArrayList(pattern);
        newPatterns.addAll(newArrayList(currentPatterns == null ? new String[0] : currentPatterns));

        final List<String> limitedNewPatterns = newPatterns.subList(0,
                Math.min(newPatterns.size(), MAXIMUM_NUMBER_OF_RECENT_SEARCHES));
        searchPageSection.put(PATTERNS_ATTRIBUTE, limitedNewPatterns.toArray(new String[0]));
    }

    List<String> getRecentPatterns() {
        final IDialogSettings searchPatternSection = dialogSettings.getSection(SEARCH_SETTINGS_SECTION_ID);
        if (searchPatternSection == null) {
            return newArrayList();
        }
        final String[] currentPatterns = searchPatternSection.getArray(PATTERNS_ATTRIBUTE);
        return newArrayList(currentPatterns == null ? new String[0] : currentPatterns);
    }

    SearchSettings readSettings() {
        final SearchSettings settings = new SearchSettings();

        final IDialogSettings searchPageSection = dialogSettings.getSection(SEARCH_SETTINGS_SECTION_ID);
        if (searchPageSection == null) {
            return settings;
        }
        settings.setSearchFor(SearchFor.valueOf(searchPageSection.get(SEARCH_FOR_ATTRIBUTE)));
        settings.setSearchLimitation(SearchLimitation.valueOf(searchPageSection.get(SEARCH_LIMIT_ATTRIBUTE)));
        settings.setTargets(getTargets(searchPageSection.getArray(SEARCH_TARGET_ATTRIBUTE)));
        settings.setCaseSensitive(searchPageSection.getBoolean(CASE_SENSITIVE_ATTRIBUTE));

        return settings;
    }

    private String[] getTargets(final SearchSettings settings) {
        return settings.getTargets().stream().map(target -> target.name()).toArray(String[]::new);
    }

    private EnumSet<SearchTarget> getTargets(final String[] array) {
        final EnumSet<SearchTarget> result = EnumSet.noneOf(SearchTarget.class);
        final List<SearchTarget> targets = Stream.of(array).map(SearchTarget::valueOf).collect(Collectors.toList());
        result.addAll(targets);
        return result;
    }
}
