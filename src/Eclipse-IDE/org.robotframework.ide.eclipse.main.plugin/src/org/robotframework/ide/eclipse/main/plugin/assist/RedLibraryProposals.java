/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.annotations.VisibleForTesting;

public class RedLibraryProposals {

    private final RobotSuiteFile suiteFile;

    private final ProposalMatcher matcher;

    public RedLibraryProposals(final RobotSuiteFile suiteFile) {
        this(suiteFile, ProposalMatchers.substringMatcher());
    }

    @VisibleForTesting
    RedLibraryProposals(final RobotSuiteFile suiteFile, final ProposalMatcher matcher) {
        this.suiteFile = suiteFile;
        this.matcher = matcher;
    }

    public List<? extends AssistProposal> getLibrariesProposals(final String userContent) {
        return getLibrariesProposals(userContent, AssistProposals.sortedByLabelsNotImportedFirst());
    }

    public List<? extends AssistProposal> getLibrariesProposals(final String userContent,
            final Comparator<? super RedLibraryProposal> comparator) {

        final List<RedLibraryProposal> proposals = new ArrayList<>();

        for (final LibrarySpecification libSpec : suiteFile.getProject().getLibrarySpecifications()) {
            final Optional<ProposalMatch> match = matcher.matches(userContent, libSpec.getName());

            if (match.isPresent()) {
                proposals.add(AssistProposals.createLibraryProposal(suiteFile, libSpec, match.get()));
            }
        }
        proposals.sort(comparator);
        return proposals;
    }
}
