/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.prefixesMatcher;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals.SettingTarget;

public class RedSettingProposalsTest {

    @Test
    public void isSettingTest() {
        assertThat(RedSettingProposals.isSetting(SettingTarget.GENERAL_TESTS, "documentation")).isTrue();
        assertThat(RedSettingProposals.isSetting(SettingTarget.GENERAL_TESTS, "metadata")).isTrue();
        assertThat(RedSettingProposals.isSetting(SettingTarget.GENERAL_TESTS, "library")).isTrue();

        assertThat(RedSettingProposals.isSetting(SettingTarget.GENERAL_TESTS, "[documentation]")).isFalse();
        assertThat(RedSettingProposals.isSetting(SettingTarget.GENERAL_TESTS, "[arguments]")).isFalse();
        assertThat(RedSettingProposals.isSetting(SettingTarget.GENERAL_TESTS, "xyz")).isFalse();

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
    public void settingDescriptionTest() {
        assertThat(RedSettingProposals.getSettingDescription(SettingTarget.GENERAL_TESTS, "documentation", ""))
                .isEqualTo("Documentation of current suite");
        assertThat(RedSettingProposals.getSettingDescription(SettingTarget.GENERAL_TESTS, "metadata", ""))
                .isEqualTo("Metadata current suite hold");
        assertThat(RedSettingProposals.getSettingDescription(SettingTarget.GENERAL_TESTS, "library", ""))
                .isEqualTo("Imports library given by its name or path");

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
    public void allGeneralSettingsProposalsAreProvided_whenInputIsEmpty() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL_TESTS)
                .getSettingsProposals("");

        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("Default Tags", "Documentation", "Force Tags", "Library", "Metadata", "Resource",
                        "Suite Setup", "Suite Teardown", "Test Setup", "Test Teardown", "Test Template", "Test Timeout",
                        "Variables");
    }

    @Test
    public void noGeneralSettingsProposalsAreProvided_whenNothingMatchesToGivenInput() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL_TESTS)
                .getSettingsProposals("Xyz");
        assertThat(proposals).isEmpty();
    }

    @Test
    public void generalSettingsProposalsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL_TESTS)
                .getSettingsProposals("Te", AssistProposals.sortedByLabelsPrefixedFirst("Te").reversed());

        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("Suite Teardown", "Suite Setup", "Test Timeout", "Test Template", "Test Teardown",
                        "Test Setup");
    }

    @Test
    public void onlyGeneralSettingsProposalsContainingInputAreProvided_whenDefaultMatcherIsUsed() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL_TESTS)
                .getSettingsProposals("es");

        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("Resource", "Test Setup", "Test Teardown", "Test Template", "Test Timeout",
                        "Variables");
    }

    @Test
    public void onlyGeneralSettingsProposalsContainingInputAreProvidedWithCorrectOrder_whenDefaultMatcherIsUsed() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL_TESTS)
                .getSettingsProposals("me");

        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("Metadata", "Documentation", "Test Timeout");
    }

    @Test
    public void onlyGeneralSettingsProposalsMatchingGivenMatcherAreProvided_whenMatcherIsGiven() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL_TESTS,
                prefixesMatcher()).getSettingsProposals("D");

        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("Default Tags", "Documentation");
    }

    @Test
    public void allKeywordSettingsProposalsAreProvided_whenInputIsEmpty() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.KEYWORD)
                .getSettingsProposals("");

        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("[Arguments]", "[Documentation]", "[Return]", "[Tags]", "[Teardown]", "[Timeout]");
    }

    @Test
    public void noKeywordSettingsProposalsAreProvided_whenNothingMatchesToGivenInput() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.KEYWORD)
                .getSettingsProposals("res");
        assertThat(proposals).isEmpty();
    }

    @Test
    public void keywordSettingsProposalsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.KEYWORD)
                .getSettingsProposals("[T", AssistProposals.sortedByLabelsPrefixedFirst("[T").reversed());

        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("[Timeout]", "[Teardown]", "[Tags]");
    }

    @Test
    public void onlyKeywordSettingsProposalsContainingInputAreProvided_whenDefaultMatcherIsUsed() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.KEYWORD)
                .getSettingsProposals("me");

        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("[Arguments]", "[Documentation]", "[Timeout]");
    }

    @Test
    public void onlyKeywordSettingsProposalsMatchingGivenMatcherAreProvided_whenMatcherIsGiven() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.KEYWORD,
                prefixesMatcher()).getSettingsProposals("[T");

        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("[Tags]", "[Teardown]", "[Timeout]");
    }

    @Test
    public void allTestCaseSettingsProposalsAreProvided_whenInputIsEmpty() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.TEST_CASE)
                .getSettingsProposals("");

        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("[Documentation]", "[Setup]", "[Tags]", "[Teardown]", "[Template]", "[Timeout]");
    }

    @Test
    public void noTestCaseSettingsProposalsAreProvided_whenNothingMatchesToGivenInput() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.TEST_CASE)
                .getSettingsProposals("res");
        assertThat(proposals).isEmpty();
    }

    @Test
    public void testCaseSettingsProposalsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.TEST_CASE)
                .getSettingsProposals("[T", AssistProposals.sortedByLabelsPrefixedFirst("[T").reversed());

        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("[Timeout]", "[Template]", "[Teardown]", "[Tags]");
    }

    @Test
    public void onlyTestCaseSettingsProposalsContainingInputAreProvided_whenDefaultMatcherIsUsed() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.TEST_CASE)
                .getSettingsProposals("me");

        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("[Documentation]", "[Timeout]");
    }

    @Test
    public void onlyTestCaseSettingsProposalsMatchingGivenMatcherAreProvided_whenMatcherIsGiven() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.TEST_CASE,
                prefixesMatcher()).getSettingsProposals("[D");

        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("[Documentation]");
    }
}
