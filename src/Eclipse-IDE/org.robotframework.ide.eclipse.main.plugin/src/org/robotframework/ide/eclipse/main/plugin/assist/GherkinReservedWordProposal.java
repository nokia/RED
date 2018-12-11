/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SingleParagraphInput;

class GherkinReservedWordProposal extends BaseAssistProposal {

    GherkinReservedWordProposal(final String word, final ProposalMatch match) {
        super(word, match);
    }

    @Override
    public boolean isDocumented() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Possible prefix for writing cases in behavior-driven style";
    }

    @Override
    public DocumentationViewInput getDocumentationInput() {
        return new SingleParagraphInput(this::getDescription);
    }
}
