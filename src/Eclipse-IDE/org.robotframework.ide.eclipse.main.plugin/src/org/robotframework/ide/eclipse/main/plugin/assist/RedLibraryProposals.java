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

import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.libraries.SitePackagesLibraries;
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

    public List<? extends AssistProposal> getSitePackagesLibrariesProposals(final String userContent) {
        return getSitePackagesLibrariesProposals(userContent,
                AssistProposals.sortedByLabelsNotImportedFirstForSitePackagesLibraries());
    }

    public List<? extends AssistProposal> getSitePackagesLibrariesProposals(final String userContent,
            final Comparator<? super RedSitePackagesLibraryProposal> comparator) {

        final RobotRuntimeEnvironment env = suiteFile.getRuntimeEnvironment();
        final SitePackagesLibraries sitePackagesLibraries = env.getSitePackagesLibrariesNames();
        final List<RedSitePackagesLibraryProposal> robotProposals = new ArrayList<>();
        final List<RedSitePackagesLibraryProposal> nonRobotProposals = new ArrayList<>();
        final List<RedSitePackagesLibraryProposal> proposals = new ArrayList<>();

        for (final String robotLib : sitePackagesLibraries.getRobotLibs()) {
            final Optional<ProposalMatch> match = matcher.matches(userContent, robotLib);

            if (match.isPresent()) {
                final RedSitePackagesLibraryProposal proposal = AssistProposals
                        .createSitePackagesLibraryProposal(robotLib, suiteFile, match.get());
                if (!proposal.isImported()) {
                    robotProposals.add(proposal);
                }
            }
        }

        for (final String nonRobotLib : sitePackagesLibraries.getNonRobotLibs()) {
            final Optional<ProposalMatch> match = matcher.matches(userContent, nonRobotLib);

            if (match.isPresent()) {
                final RedSitePackagesLibraryProposal proposal = AssistProposals
                        .createSitePackagesLibraryProposal(nonRobotLib, suiteFile, match.get());
                if (!proposal.isImported()) {
                    nonRobotProposals.add(proposal);
                }
            }
        }
        robotProposals.sort(comparator);
        nonRobotProposals.sort(comparator);
        proposals.addAll(robotProposals);
        proposals.addAll(nonRobotProposals);
        return proposals;
    }
}
