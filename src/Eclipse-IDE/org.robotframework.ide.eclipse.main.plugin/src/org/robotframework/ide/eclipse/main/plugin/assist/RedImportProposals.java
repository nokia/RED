/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.robotframework.ide.eclipse.main.plugin.assist.AssistProposals.sortedByLabelsPrefixedFirst;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.robotframework.ide.eclipse.main.plugin.assist.BddMatchesHelper.BddAwareProposalMatch;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Files;

public class RedImportProposals {

    private final RobotSuiteFile suiteFile;

    private final ProposalMatcher matcher;

    public RedImportProposals(final RobotSuiteFile suiteFile) {
        this(suiteFile, ProposalMatchers.substringMatcher());
    }

    @VisibleForTesting
    RedImportProposals(final RobotSuiteFile suiteFile, final ProposalMatcher matcher) {
        this.suiteFile = suiteFile;
        this.matcher = matcher;
    }

    public List<? extends AssistProposal> getImportsProposals(final String userContent) {
        return getImportsProposals(userContent, sortedByLabelsPrefixedFirst(userContent));
    }

    public List<? extends AssistProposal> getImportsProposals(final String userContent,
            final Comparator<? super RedImportProposal> comparator) {

        final List<RedImportProposal> proposals = new ArrayList<>();

        final Map<LibrarySpecification, Collection<Optional<String>>> libs = suiteFile.getImportedLibraries().asMap();

        for (final Entry<LibrarySpecification, Collection<Optional<String>>> entry : libs.entrySet()) {
            for (final Optional<String> alias : entry.getValue()) {
                final String nameToUse = alias.orElse(entry.getKey().getName());

                final BddMatchesHelper bddHelper = new BddMatchesHelper(matcher);
                final BddAwareProposalMatch match = bddHelper.findBddAwareMatch(userContent, nameToUse);

                if (match.getMatch().isPresent()) {
                    proposals.add(AssistProposals.createLibraryImportInCodeProposal(nameToUse, match.getBddPrefix(),
                            match.getMatch().get()));
                }
            }
        }

        final ArrayList<RedImportProposal> resProposals = new ArrayList<>();
        for (final String path : suiteFile.getResourcesPaths()) {
            final String nameToUse = Files.getNameWithoutExtension(path);

            final BddMatchesHelper bddHelper = new BddMatchesHelper(matcher);
            final BddAwareProposalMatch match = bddHelper.findBddAwareMatch(userContent, nameToUse);

            if (match.getMatch().isPresent()) {
                resProposals.add(AssistProposals.createResourceImportInCodeProposal(nameToUse, match.getBddPrefix(),
                        match.getMatch().get()));
            }
        }
        proposals.sort(comparator);
        resProposals.sort(comparator);
        proposals.addAll(resProposals);
        return proposals;
    }
}
