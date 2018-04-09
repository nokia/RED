/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.prefixesMatcher;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.reverseComparator;

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
    public void settingDescriptionTest() {
        assertThat(RedSettingProposals.getSettingDescription(SettingTarget.GENERAL, "documentation", ""))
                .isEqualTo("Documentation of current suite");
        assertThat(RedSettingProposals.getSettingDescription(SettingTarget.GENERAL, "metadata", ""))
                .isEqualTo("Metadata current suite hold");
        assertThat(RedSettingProposals.getSettingDescription(SettingTarget.GENERAL, "library", ""))
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
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL)
                .getSettingsProposals("");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("Default Tags", "Documentation",
                "Force Tags", "Library", "Metadata", "Resource", "Suite Setup", "Suite Teardown", "Test Setup",
                "Test Teardown", "Test Template", "Test Timeout", "Variables");
    }

    @Test
    public void noGeneralSettingsProposalsAreProvided_whenNothingMatchesToGivenInput() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL)
                .getSettingsProposals("Xyz");
        assertThat(proposals).isEmpty();
    }

    @Test
    public void generalSettingsProposalsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL)
                .getSettingsProposals("Te", reverseComparator(AssistProposals.sortedByLabelsPrefixedFirst("Te")));

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("Suite Teardown", "Suite Setup",
                "Test Timeout", "Test Template", "Test Teardown", "Test Setup");
    }

    @Test
    public void onlyGeneralSettingsProposalsContainingInputAreProvided_whenDefaultMatcherIsUsed() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL)
                .getSettingsProposals("es");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("Resource", "Test Setup",
                "Test Teardown", "Test Template", "Test Timeout", "Variables");
    }

    @Test
    public void onlyGeneralSettingsProposalsContainingInputAreProvidedWithCorrectOrder_whenDefaultMatcherIsUsed() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL)
                .getSettingsProposals("me");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("Metadata", "Documentation",
                "Test Timeout");
    }

    @Test
    public void onlyGeneralSettingsProposalsMatchingGivenMatcherAreProvided_whenMatcherIsGiven() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.GENERAL,
                prefixesMatcher()).getSettingsProposals("D");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("Default Tags", "Documentation");
    }

    @Test
    public void allKeywordSettingsProposalsAreProvided_whenInputIsEmpty() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.KEYWORD)
                .getSettingsProposals("");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("[Arguments]", "[Documentation]",
                "[Return]", "[Tags]", "[Teardown]", "[Timeout]");
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
                .getSettingsProposals("[T", reverseComparator(AssistProposals.sortedByLabelsPrefixedFirst("[T")));

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("[Timeout]", "[Teardown]", "[Tags]");
    }

    @Test
    public void onlyKeywordSettingsProposalsContainingInputAreProvided_whenDefaultMatcherIsUsed() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.KEYWORD)
                .getSettingsProposals("me");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("[Arguments]", "[Documentation]",
                "[Timeout]");
    }

    @Test
    public void onlyKeywordSettingsProposalsMatchingGivenMatcherAreProvided_whenMatcherIsGiven() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.KEYWORD,
                prefixesMatcher()).getSettingsProposals("[T");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("[Tags]", "[Teardown]", "[Timeout]");
    }

    @Test
    public void allTestCaseSettingsProposalsAreProvided_whenInputIsEmpty() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.TEST_CASE)
                .getSettingsProposals("");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("[Documentation]", "[Setup]",
                "[Tags]", "[Teardown]", "[Template]", "[Timeout]");
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
                .getSettingsProposals("[T", reverseComparator(AssistProposals.sortedByLabelsPrefixedFirst("[T")));

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("[Timeout]", "[Template]",
                "[Teardown]", "[Tags]");
    }

    @Test
    public void onlyTestCaseSettingsProposalsContainingInputAreProvided_whenDefaultMatcherIsUsed() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.TEST_CASE)
                .getSettingsProposals("me");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("[Documentation]", "[Timeout]");
    }

    @Test
    public void onlyTestCaseSettingsProposalsMatchingGivenMatcherAreProvided_whenMatcherIsGiven() {
        final List<? extends AssistProposal> proposals = new RedSettingProposals(SettingTarget.TEST_CASE,
                prefixesMatcher()).getSettingsProposals("[D");

        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("[Documentation]");
    }
}
