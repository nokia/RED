/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.transform;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals.SettingTarget;

public class RedSettingProposalsTest {

    @Test
    public void isSettingTest() {
        assertThat(RedSettingProposals.isSetting(SettingTarget.GENERAL, "documentation")).isTrue();
        assertThat(RedSettingProposals.isSetting(SettingTarget.GENERAL, "metadata")).isTrue();
        assertThat(RedSettingProposals.isSetting(SettingTarget.GENERAL, "library")).isTrue();

        assertThat(RedSettingProposals.isSetting(SettingTarget.GENERAL, "[documentation]")).isFalse();
        assertThat(RedSettingProposals.isSetting(SettingTarget.GENERAL, "[arguments]")).isFalse();
        assertThat(RedSettingProposals.isSetting(SettingTarget.GENERAL, "xyz")).isFalse();

        assertThat(RedSettingProposals.isSetting(SettingTarget.KEYWORD, "[documentation]")).isTrue();
        assertThat(RedSettingProposals.isSetting(SettingTarget.KEYWORD, "[arguments]")).isTrue();
        assertThat(RedSettingProposals.isSetting(SettingTarget.KEYWORD, "[tags]")).isTrue();

        assertThat(RedSettingProposals.isSetting(SettingTarget.KEYWORD, "documentation")).isFalse();
        assertThat(RedSettingProposals.isSetting(SettingTarget.KEYWORD, "[template]")).isFalse();
        assertThat(RedSettingProposals.isSetting(SettingTarget.KEYWORD, "xyz")).isFalse();

        assertThat(RedSettingProposals.isSetting(SettingTarget.TEST_CASE, "[documentation]")).isTrue();
        assertThat(RedSettingProposals.isSetting(SettingTarget.TEST_CASE, "[template]")).isTrue();
        assertThat(RedSettingProposals.isSetting(SettingTarget.TEST_CASE, "[tags]")).isTrue();

        assertThat(RedSettingProposals.isSetting(SettingTarget.TEST_CASE, "documentation")).isFalse();
        assertThat(RedSettingProposals.isSetting(SettingTarget.TEST_CASE, "[arguments]")).isFalse();
        assertThat(RedSettingProposals.isSetting(SettingTarget.TEST_CASE, "xyz")).isFalse();
    }

    @Test
    public void settingDesciprionTest() {
        assertThat(RedSettingProposals.getSettingDescription(SettingTarget.GENERAL, "documentation", "")).isEmpty();
        assertThat(RedSettingProposals.getSettingDescription(SettingTarget.GENERAL, "metadata", "")).isEmpty();
        assertThat(RedSettingProposals.getSettingDescription(SettingTarget.GENERAL, "library", "")).isEmpty();

        assertThat(RedSettingProposals.getSettingDescription(SettingTarget.KEYWORD, "[documentation]", ""))
                .isNotEmpty();
        assertThat(RedSettingProposals.getSettingDescription(SettingTarget.KEYWORD, "[arguments]", "")).isNotEmpty();
        assertThat(RedSettingProposals.getSettingDescription(SettingTarget.KEYWORD, "[tags]", "")).isNotEmpty();

        assertThat(RedSettingProposals.getSettingDescription(SettingTarget.TEST_CASE, "[documentation]", ""))
                .isNotEmpty();
        assertThat(RedSettingProposals.getSettingDescription(SettingTarget.TEST_CASE, "[template]", "")).isNotEmpty();
        assertThat(RedSettingProposals.getSettingDescription(SettingTarget.TEST_CASE, "[tags]", "")).isNotEmpty();
    }

    @Test
    public void allGeneralSettingsProposalsAreProvided_whenPrefixIsEmpty() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL)
                .getSettingsProposals("");

        assertThat(transform(proposals, Commons.toLabels())).containsExactly("Default Tags", "Documentation",
                "Force Tags", "Library", "Metadata", "Resource", "Suite Setup", "Suite Teardown", "Test Setup",
                "Test Teardown", "Test Template", "Test Timeout", "Variables");
    }

    @Test
    public void noGeneralSettingsProposalsAreProvided_whenNothingMatchesToGivenPrefix() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL)
                .getSettingsProposals("Xyz");
        assertThat(proposals).isEmpty();
    }

    @Test
    public void generalSettingsProposalsAreProvidedInOrderInducedByGivenComparator() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL)
                .getSettingsProposals("T", Commons.reverseComparator(AssistProposals.sortedByLabels()));

        assertThat(transform(proposals, Commons.toLabels())).containsExactly("Test Timeout", "Test Template",
                "Test Teardown", "Test Setup");
    }

    @Test
    public void onlyGeneralSettingsProposalsMatchingGivenMatcherAreProvided_whenMatcherIsGiven() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL,
                Commons.substringMatcher()).getSettingsProposals("es");

        assertThat(transform(proposals, Commons.toLabels())).containsExactly("Resource", "Test Setup", "Test Teardown",
                "Test Template", "Test Timeout", "Variables");
    }

    @Test
    public void allKeywordSettingsProposalsAreProvided_whenPrefixIsEmpty() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.KEYWORD)
                .getSettingsProposals("");

        assertThat(transform(proposals, Commons.toLabels())).containsExactly("[Arguments]", "[Documentation]",
                "[Return]", "[Tags]", "[Teardown]", "[Timeout]");
    }

    @Test
    public void noKeywordSettingsProposalsAreProvided_whenNothingMatchesToGivenPrefix() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.KEYWORD)
                .getSettingsProposals("docu");
        assertThat(proposals).isEmpty();
    }

    @Test
    public void keywordSettingsProposalsAreProvidedInOrderInducedByGivenComparator() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.KEYWORD)
                .getSettingsProposals("[T", Commons.reverseComparator(AssistProposals.sortedByLabels()));

        assertThat(transform(proposals, Commons.toLabels())).containsExactly("[Timeout]", "[Teardown]", "[Tags]");
    }

    @Test
    public void onlyKeywordSettingsProposalsMatchingGivenMatcherAreProvided_whenMatcherIsGiven() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.KEYWORD,
                Commons.substringMatcher()).getSettingsProposals("me");

        assertThat(transform(proposals, Commons.toLabels())).containsExactly("[Arguments]", "[Documentation]",
                "[Timeout]");
    }

    @Test
    public void allTestCaseSettingsProposalsAreProvided_whenPrefixIsEmpty() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.TEST_CASE)
                .getSettingsProposals("");

        assertThat(transform(proposals, Commons.toLabels())).containsExactly("[Documentation]", "[Setup]", "[Tags]",
                "[Teardown]", "[Template]", "[Timeout]");
    }

    @Test
    public void noTestCaseSettingsProposalsAreProvided_whenNothingMatchesToGivenPrefix() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.TEST_CASE)
                .getSettingsProposals("docu");
        assertThat(proposals).isEmpty();
    }

    @Test
    public void testCaseSettingsProposalsAreProvidedInOrderInducedByGivenComparator() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.TEST_CASE)
                .getSettingsProposals("[T", Commons.reverseComparator(AssistProposals.sortedByLabels()));

        assertThat(transform(proposals, Commons.toLabels())).containsExactly("[Timeout]", "[Template]", "[Teardown]",
                "[Tags]");
    }

    @Test
    public void onlyTestCaseSettingsProposalsMatchingGivenMatcherAreProvided_whenMatcherIsGiven() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.TEST_CASE,
                Commons.substringMatcher()).getSettingsProposals("me");

        assertThat(transform(proposals, Commons.toLabels())).containsExactly("[Documentation]", "[Timeout]");
    }
}
