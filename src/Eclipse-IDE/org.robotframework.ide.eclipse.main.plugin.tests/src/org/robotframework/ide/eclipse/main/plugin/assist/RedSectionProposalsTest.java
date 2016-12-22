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

public class RedSectionProposalsTest {

    @Test
    public void allProposalsAreProvided_whenPrefixIsEmpty() {
        final List<? extends AssistProposal> proposals = new RedSectionProposals().getSectionsProposals("");
        
        assertThat(transform(proposals, Commons.toLabels())).containsExactly("*** Keywords ***", "*** Settings ***",
                "*** Test Cases ***", "*** Variables ***");
    }

    @Test
    public void noProposalsAreProvided_whenNothingMatchesToGivenPrefix() {
        final List<? extends AssistProposal> proposals = new RedSectionProposals().getSectionsProposals("*Section");
        assertThat(proposals).isEmpty();
    }

    @Test
    public void proposalsAreProvidedInOrderInducedByGivenComparator() {
        final List<? extends AssistProposal> proposals = new RedSectionProposals().getSectionsProposals("*",
                Commons.reverseComparator(AssistProposals.sortedByLabels()));

        assertThat(transform(proposals, Commons.toLabels())).containsExactly("*** Variables ***", "*** Test Cases ***",
                "*** Settings ***", "*** Keywords ***");
    }

    @Test
    public void onlyProposalsMatchingGivenMatcherAreProvided_whenMatcherIsGiven() {
        final List<? extends AssistProposal> proposals = new RedSectionProposals(Commons.substringMatcher())
                .getSectionsProposals("es");

        assertThat(transform(proposals, Commons.toLabels())).containsExactly("*** Test Cases ***", "*** Variables ***");
    }
}
