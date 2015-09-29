/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

public class RedKeywordProposals {

    private final RobotSuiteFile suiteFile;

    public RedKeywordProposals(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    public static Comparator<RedKeywordProposal> sortedBySourcesAndNames() {
        return new Comparator<RedKeywordProposal>() {
            @Override
            public int compare(final RedKeywordProposal proposal1, final RedKeywordProposal proposal2) {
                if (proposal1.getType() == proposal2.getType()) {
                    if (proposal1.getSourceName().equals(proposal2.getSourceName())) {
                        return proposal1.getLabel().compareTo(proposal2.getLabel());
                    } else {
                        return proposal1.getSourceName().compareTo(proposal2.getSourceName());
                    }
                } else {
                    return proposal1.getType().compareTo(proposal2.getType());
                }
            }
        };
    }

    public static Comparator<RedKeywordProposal> sortedByNames() {
        return new Comparator<RedKeywordProposal>() {
            @Override
            public int compare(final RedKeywordProposal proposal1, final RedKeywordProposal proposal2) {
                return proposal1.getLabel().compareTo(proposal2.getLabel());
            }
        };
    }

    public List<RedKeywordProposal> getKeywordProposals() {
        return getKeywordProposals("", null);
    }

    public List<RedKeywordProposal> getKeywordProposals(final Comparator<RedKeywordProposal> comparator) {
        return getKeywordProposals("", comparator);
    }

    public List<RedKeywordProposal> getKeywordProposals(final String prefix) {
        return getKeywordProposals(prefix, null);
    }

    public List<RedKeywordProposal> getKeywordProposals(final String prefix,
            final Comparator<RedKeywordProposal> comparator) {

        final List<RedKeywordProposal> proposals = newArrayList();

        new KeywordDefinitionLocator(suiteFile).locateKeywordDefinition(new KeywordDetector() {

            @Override
            public ContinueDecision libraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec) {
                if (kwSpec.getName().startsWith(prefix)) {
                    proposals.add(RedKeywordProposal.create(libSpec, kwSpec));
                }
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision keywordDetected(final RobotSuiteFile file, final RobotKeywordDefinition keyword) {
                if (keyword.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                    if (file == suiteFile) {
                        proposals.add(RedKeywordProposal.create(keyword));
                    } else {
                        proposals.add(RedKeywordProposal.createExternal(file, keyword));
                    }
                }
                return ContinueDecision.CONTINUE;
            }
        });

        if (comparator != null) {
            Collections.sort(proposals, comparator);
        }
        return proposals;
    }
}
