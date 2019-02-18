/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.List;
import java.util.Optional;

import org.rf.ide.core.testdata.model.table.keywords.names.CamelCaseKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.variables.AVariable;

import com.google.common.collect.Range;

public class ProposalMatchers {

    public static ProposalMatcher substringMatcher() {
        return (userContent, proposalContent) -> {
            final String lowerCaseProposalContent = proposalContent.toLowerCase();
            final String lowerCaseUserContent = userContent.toLowerCase();
            if (lowerCaseProposalContent.contains(lowerCaseUserContent)) {
                final int index = lowerCaseProposalContent.indexOf(lowerCaseUserContent);
                return Optional.of(new ProposalMatch(Range.closedOpen(index, index + userContent.length())));
            }
            return Optional.empty();
        };
    }

    public static ProposalMatcher keywordsMatcher() {
        return (userContent, proposalContent) -> {
            final List<Range<Integer>> ranges = CamelCaseKeywordNamesSupport.matches(proposalContent, userContent);
            if (!ranges.isEmpty()) {
                return Optional.of(new ProposalMatch(ranges));
            }
            return EmbeddedKeywordNamesSupport.containsIgnoreCase(proposalContent, userContent).map(ProposalMatch::new);
        };
    }

    public static ProposalMatcher variablesMatcher() {
        return (userContent, proposalContent) -> {
            final String varIdentificator = userContent.length() > 0 ? userContent.substring(0, 1) : "";
            if (!AVariable.ROBOT_VAR_IDENTIFICATORS.contains(varIdentificator)) {
                return substringMatcher().matches(userContent, proposalContent);
            }

            final String lowerCaseVarNamePart = userContent.length() > 2 ? userContent.substring(2).toLowerCase() : "";
            final String lowerCaseProposalContent = proposalContent.toLowerCase();
            if (proposalContent.startsWith(varIdentificator)
                    && lowerCaseProposalContent.contains(lowerCaseVarNamePart)) {
                final int index = lowerCaseProposalContent.indexOf(lowerCaseVarNamePart);
                return Optional.of(new ProposalMatch(Range.closedOpen(index, index + lowerCaseVarNamePart.length())));
            }
            return Optional.empty();
        };
    }
}
