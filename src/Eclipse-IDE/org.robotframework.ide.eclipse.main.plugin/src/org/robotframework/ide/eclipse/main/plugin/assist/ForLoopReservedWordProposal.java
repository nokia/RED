/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.Optional;

import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SingleParagraphInput;

class ForLoopReservedWordProposal extends BaseAssistProposal {

    ForLoopReservedWordProposal(final String word, final ProposalMatch match) {
        super(word, match);
    }

    @Override
    public boolean isDocumented() {
        return true;
    }

    @Override
    public String getDescription() {
        return Optional.ofNullable(ForLoopReservedWordsProposals.DESCRIPTIONS.get(content)).orElse("");
    }

    @Override
    public DocumentationViewInput getDocumentationInput() {
        return new SingleParagraphInput(this::getDescription);
    }
}
