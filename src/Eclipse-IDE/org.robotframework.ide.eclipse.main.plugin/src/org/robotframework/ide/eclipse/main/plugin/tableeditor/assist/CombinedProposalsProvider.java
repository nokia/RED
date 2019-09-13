/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposalProvider;

public class CombinedProposalsProvider implements RedContentProposalProvider {

    private final List<RedContentProposalProvider> providers;

    public CombinedProposalsProvider(final RedContentProposalProvider... proposalProviders) {
        this.providers = newArrayList(proposalProviders);
    }

    @Override
    public boolean shouldShowProposals(final AssistantContext context) {
        return true;
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {
        final List<RedContentProposal> proposals = new ArrayList<>();
        for (final RedContentProposalProvider provider : providers) {
            if (provider.shouldShowProposals(context)) {
                for (final RedContentProposal proposal : provider.getProposals(contents, position, context)) {
                    proposals.add(proposal);
                }
            }
        }
        return proposals.toArray(new RedContentProposal[0]);
    }
}
