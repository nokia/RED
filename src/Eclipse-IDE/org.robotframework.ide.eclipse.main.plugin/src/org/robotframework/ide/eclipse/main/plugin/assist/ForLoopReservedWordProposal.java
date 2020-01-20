/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.robotframework.red.jface.viewers.Stylers.mixingStyler;

import java.util.Optional;

import org.eclipse.jface.viewers.StyledString;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SingleParagraphInput;
import org.robotframework.red.jface.viewers.Stylers;

import com.google.common.collect.Range;

class ForLoopReservedWordProposal extends BaseAssistProposal {

    private boolean isDeprecated;

    ForLoopReservedWordProposal(final String word, final ProposalMatch match, final boolean isDeprecated) {
        super(word, match);

        this.isDeprecated = isDeprecated;
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
    
    @Override
    public StyledString getStyledLabel() {
        final StyledString label = isDeprecated ? new StyledString(getLabel(), Stylers.Common.STRIKEOUT_STYLER)
                : new StyledString(getLabel());
        for (final Range<Integer> matchingRange : match) {
            final int length = Math.min(matchingRange.upperEndpoint() - matchingRange.lowerEndpoint(),
                    label.length() - matchingRange.lowerEndpoint());
            label.setStyle(matchingRange.lowerEndpoint(), length,
                    isDeprecated ? mixingStyler(Stylers.Common.STRIKEOUT_STYLER, Stylers.Common.MATCH_DECORATION_STYLER)
                            : Stylers.Common.MATCH_DECORATION_STYLER);
        }
        return label;
    }
}
