/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.search.SearchSettings.SearchFor;
import org.robotframework.ide.eclipse.main.plugin.search.SearchSettings.SearchLimitation;
import org.robotframework.ide.eclipse.main.plugin.search.SearchSettings.SearchTarget;

public class SearchSettingsPersisterTest {

    @Test
    public void recentPatternIsStored() {
        final IDialogSettings dialogSettings = new DialogSettings("settings");
        final SearchSettingsPersister persister = new SearchSettingsPersister(dialogSettings);

        final SearchSettings settings = new SearchSettings();
        settings.getSearchPattern().setPattern("pattern");

        persister.writeSettings(settings);

        final IDialogSettings searchSettingsSection = dialogSettings.getSection(SearchSettingsPersister.SEARCH_SETTINGS_SECTION_ID);
        assertThat(searchSettingsSection.getArray(SearchSettingsPersister.PATTERNS_ATTRIBUTE))
                .containsExactly("pattern");
    }

    @Test
    public void searchForIsStored() {
        final IDialogSettings dialogSettings = new DialogSettings("settings");
        final SearchSettingsPersister persister = new SearchSettingsPersister(dialogSettings);

        final SearchSettings settings = new SearchSettings();
        settings.setSearchFor(SearchFor.VARIABLE);

        persister.writeSettings(settings);

        final IDialogSettings searchSettingsSection = dialogSettings
                .getSection(SearchSettingsPersister.SEARCH_SETTINGS_SECTION_ID);
        assertThat(searchSettingsSection.get(SearchSettingsPersister.SEARCH_FOR_ATTRIBUTE))
                .isEqualTo(SearchFor.VARIABLE.name());
    }

    @Test
    public void searchLimitationIsStored() {
        final IDialogSettings dialogSettings = new DialogSettings("settings");
        final SearchSettingsPersister persister = new SearchSettingsPersister(dialogSettings);

        final SearchSettings settings = new SearchSettings();
        settings.setSearchLimitation(SearchLimitation.NO_LIMITS);

        persister.writeSettings(settings);

        final IDialogSettings searchSettingsSection = dialogSettings
                .getSection(SearchSettingsPersister.SEARCH_SETTINGS_SECTION_ID);
        assertThat(searchSettingsSection.get(SearchSettingsPersister.SEARCH_LIMIT_ATTRIBUTE))
                .isEqualTo(SearchLimitation.NO_LIMITS.name());
    }

    @Test
    public void caseSensitivityIsStored() {
        final IDialogSettings dialogSettings = new DialogSettings("settings");
        final SearchSettingsPersister persister = new SearchSettingsPersister(dialogSettings);

        final SearchSettings settings = new SearchSettings();
        settings.setCaseSensitive(true);

        persister.writeSettings(settings);

        final IDialogSettings searchSettingsSection = dialogSettings
                .getSection(SearchSettingsPersister.SEARCH_SETTINGS_SECTION_ID);
        assertThat(searchSettingsSection.getBoolean(SearchSettingsPersister.CASE_SENSITIVE_ATTRIBUTE)).isTrue();
    }

    @Test
    public void searchTargetsAreStored() {
        final IDialogSettings dialogSettings = new DialogSettings("settings");
        final SearchSettingsPersister persister = new SearchSettingsPersister(dialogSettings);

        final SearchSettings settings = new SearchSettings();
        settings.setTargets(EnumSet.of(SearchTarget.STANDARD_LIBRARY, SearchTarget.REFERENCED_LIBRARY));

        persister.writeSettings(settings);

        final IDialogSettings searchSettingsSection = dialogSettings
                .getSection(SearchSettingsPersister.SEARCH_SETTINGS_SECTION_ID);
        assertThat(searchSettingsSection.getArray(SearchSettingsPersister.SEARCH_TARGET_ATTRIBUTE))
                .containsOnly(SearchTarget.STANDARD_LIBRARY.name(), SearchTarget.REFERENCED_LIBRARY.name());
    }

    @Test
    public void recentPatternsAreLimitedAndStoredInLifoOrder() {
        final SearchSettingsPersister persister = new SearchSettingsPersister(new DialogSettings("settings"));

        for (int i = 1; i <= 100; i++) {
            final SearchSettings settings = new SearchSettings();
            settings.getSearchPattern().setPattern("p" + i);

            persister.writeSettings(settings);
            
            final int expectedNoOfPatterns = Math.min(i, SearchSettingsPersister.MAXIMUM_NUMBER_OF_RECENT_SEARCHES);
            assertThat(persister.getRecentPatterns().size()).isEqualTo(expectedNoOfPatterns);
        }

        final List<String> recentPatterns = persister.getRecentPatterns();
        assertThat(recentPatterns).containsExactly("p100", "p99", "p98", "p97", "p96", "p95", "p94", "p93", "p92",
                "p91", "p90", "p89", "p88", "p87", "p86", "p85", "p84", "p83", "p82", "p81");
    }

    @Test
    public void defaultSettingsAreReturned_whenNoSettingsAreStored() {
        final SearchSettingsPersister persister = new SearchSettingsPersister(new DialogSettings("settings"));

        final SearchSettings settings = persister.readSettings();
        final SearchSettings defaultSettings = new SearchSettings();
        
        assertThat(settings.isCaseSensitive()).isEqualTo(defaultSettings.isCaseSensitive());
        assertThat(settings.getSearchFor()).isEqualTo(defaultSettings.getSearchFor());
        assertThat(settings.getSearchLimitation()).isEqualTo(defaultSettings.getSearchLimitation());
        assertThat(settings.getTargets()).isEqualTo(defaultSettings.getTargets());
        assertThat(settings.getSearchPattern()).isEqualTo(defaultSettings.getSearchPattern());
    }

    @Test
    public void thereAreNoRecentPatterns_whenNoSettingsAreStored() {
        final SearchSettingsPersister persister = new SearchSettingsPersister(new DialogSettings("settings"));

        assertThat(persister.getRecentPatterns()).isEmpty();
    }

    @Test
    public void thereAreNoRecentPatterns_whenNoPatternsAreStored() {
        final DialogSettings dialogSettings = new DialogSettings("settings");
        dialogSettings.addNewSection(SearchSettingsPersister.SEARCH_SETTINGS_SECTION_ID);

        final SearchSettingsPersister persister = new SearchSettingsPersister(dialogSettings);

        assertThat(persister.getRecentPatterns()).isEmpty();
    }

    @Test
    public void properSettingsAreReturned_whenSettingsAreAlreadyStored() {
        final IDialogSettings dialogSettings = new DialogSettings("settings");
        final IDialogSettings section = dialogSettings.addNewSection(SearchSettingsPersister.SEARCH_SETTINGS_SECTION_ID);
        section.put(SearchSettingsPersister.CASE_SENSITIVE_ATTRIBUTE, true);
        section.put(SearchSettingsPersister.SEARCH_FOR_ATTRIBUTE, SearchFor.DOC_CONTENT.name());
        section.put(SearchSettingsPersister.SEARCH_LIMIT_ATTRIBUTE, SearchLimitation.NO_LIMITS.name());
        section.put(SearchSettingsPersister.SEARCH_TARGET_ATTRIBUTE,
                new String[] { SearchTarget.RESOURCE.name(), SearchTarget.SUITE.name() });

        final SearchSettingsPersister persister = new SearchSettingsPersister(dialogSettings);

        final SearchSettings settings = persister.readSettings();

        assertThat(settings.isCaseSensitive()).isEqualTo(true);
        assertThat(settings.getSearchFor()).isEqualTo(SearchFor.DOC_CONTENT);
        assertThat(settings.getSearchLimitation()).isEqualTo(SearchLimitation.NO_LIMITS);
        assertThat(settings.getTargets()).containsOnly(SearchTarget.RESOURCE, SearchTarget.SUITE);
    }
}
