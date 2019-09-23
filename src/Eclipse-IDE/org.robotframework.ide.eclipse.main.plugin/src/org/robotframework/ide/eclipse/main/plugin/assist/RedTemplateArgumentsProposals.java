/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class RedTemplateArgumentsProposals {

    private final RobotSuiteFile suiteFile;

    public RedTemplateArgumentsProposals(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    public List<RedTemplateArgumentsProposal> getRedTemplateArgumentsProposal(final String templateKeyword) {
        final List<RedTemplateArgumentsProposal> proposals = new ArrayList<>();
        if (EmbeddedKeywordNamesSupport.hasEmbeddedArguments(templateKeyword)) {
            proposals.add(createKeywordWithEmbeddedArgumentsProposal(templateKeyword));
        } else {
            final Optional<RedKeywordProposal> kwProposal = new RedKeywordProposals(suiteFile)
                    .getBestMatchingKeywordProposal(templateKeyword);
            if (kwProposal.isPresent()) {
                final ArgumentsDescriptor argumentsDescriptor = kwProposal.get().getArgumentsDescriptor();
                proposals.add(new RedTemplateArgumentsProposal(templateKeyword, argumentsDescriptor));
            }
        }
        return proposals;
    }

    private RedTemplateArgumentsProposal createKeywordWithEmbeddedArgumentsProposal(final String templateKeyword) {
        final List<String> arguments = EmbeddedKeywordNamesSupport.findEmbeddedArgumentsRanges(templateKeyword)
                .asRanges()
                .stream()
                .map(range -> templateKeyword.substring(range.lowerEndpoint() + 2, range.upperEndpoint()))
                .map(String::trim)
                .collect(Collectors.toList());
        final ArgumentsDescriptor argumentsDescriptor = ArgumentsDescriptor.createDescriptor(arguments);
        return new RedTemplateArgumentsProposal(templateKeyword, argumentsDescriptor);
    }
}
