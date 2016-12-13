/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;
import com.google.common.io.Files;

public class RedImportProposals {

    private final RobotSuiteFile suiteFile;

    private final ProposalMatcher matcher;

    public RedImportProposals(final RobotSuiteFile suiteFile) {
        this(suiteFile, ProposalMatchers.prefixesMatcher());
    }

    public RedImportProposals(final RobotSuiteFile suiteFile, final ProposalMatcher matcher) {
        this.suiteFile = suiteFile;
        this.matcher = matcher;
    }

    public List<? extends AssistProposal> getImportsProposals(final String userContent) {
        return getImportsProposals(userContent, AssistProposals.sortedByLabels());
    }

    public List<? extends AssistProposal> getImportsProposals(final String userContent,
            final Comparator<? super RedImportProposal> comparator) {

        final ArrayList<RedImportProposal> proposals = new ArrayList<>();

        final Map<LibrarySpecification, Collection<String>> libs = suiteFile.getImportedLibraries().asMap();

        for (final Entry<LibrarySpecification, Collection<String>> entry : libs.entrySet()) {
            for (final String name : entry.getValue()) {
                final String nameToUse = name.isEmpty() ? entry.getKey().getName() : name;
                final Optional<ProposalMatch> match = matcher.matches(userContent, nameToUse);

                if (match.isPresent()) {
                    proposals.add(AssistProposals.createLibraryImportInCodeProposal(nameToUse, match));
                }
            }
        }

        final ArrayList<RedImportProposal> resProposals = new ArrayList<>();
        for (final String path : suiteFile.getResourcesPaths()) {
            final String nameToUse = Files.getNameWithoutExtension(path);
            final Optional<ProposalMatch> match = matcher.matches(userContent, nameToUse);

            if (match.isPresent()) {
                resProposals.add(AssistProposals.createResourceImportInCodeProposal(nameToUse, match));
            }
        }
        proposals.sort(comparator);
        resProposals.sort(comparator);
        proposals.addAll(resProposals);
        return proposals;
    }
}
