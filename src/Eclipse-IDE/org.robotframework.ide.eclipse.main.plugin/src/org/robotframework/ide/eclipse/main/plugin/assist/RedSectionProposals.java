/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.ide.eclipse.main.plugin.assist.AssistProposals.sortedByLabels;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;

import com.google.common.base.Optional;

public class RedSectionProposals {

    private static final List<String> SECTION_NAMES = newArrayList(RobotKeywordsSection.SECTION_NAME,
            RobotCasesSection.SECTION_NAME, RobotSettingsSection.SECTION_NAME, RobotVariablesSection.SECTION_NAME);

    private final ProposalMatcher matcher;

    public RedSectionProposals() {
        this(ProposalMatchers.prefixesMatcher());
    }

    public RedSectionProposals(final ProposalMatcher matcher) {
        this.matcher = matcher;
    }

    public List<? extends AssistProposal> getSectionsProposals(final String userContent) {
        return getSectionsProposals(userContent, sortedByLabels());
    }

    public List<? extends AssistProposal> getSectionsProposals(final String userContent,
            final Comparator<? super RedSectionProposal> comparator) {

        final List<RedSectionProposal> proposals = new ArrayList<>();

        for (final String sectionName : SECTION_NAMES) {
            final Optional<ProposalMatch> match = matcher.matches(userContent, "*** " + sectionName + " ***");

            if (match.isPresent()) {
                proposals.add(AssistProposals.createSectionProposal(sectionName, match.get()));
            }
        }
        proposals.sort(comparator);
        return proposals;
    }
}
