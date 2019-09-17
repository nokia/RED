/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.assist;

public interface RedContentProposalProvider {

    boolean shouldShowProposals(AssistantContext context);

    RedContentProposal[] getProposals(String contents, int position, AssistantContext context);

    default RedContentProposal[] computeProposals(final String contents, final int position,
            final AssistantContext context) {
        if (shouldShowProposals(context)) {
            return getProposals(contents, position, context);
        } else {
            return null;
        }
    }
}
