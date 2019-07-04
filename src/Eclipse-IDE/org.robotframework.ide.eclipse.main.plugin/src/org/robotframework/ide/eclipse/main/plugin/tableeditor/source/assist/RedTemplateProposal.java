/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.assist.ProposalMatch;
import org.robotframework.red.jface.viewers.Stylers;

import com.google.common.collect.Range;

public class RedTemplateProposal extends TemplateProposal
        implements ICompletionProposalExtension4, ICompletionProposalExtension6 {

    private final ProposalMatch match;

    public RedTemplateProposal(final Template template, final TemplateContext context, final IRegion region,
            final Image image, final ProposalMatch match) {
        super(template, context, region, image);
        this.match = match;
    }

    @Override
    public boolean isAutoInsertable() {
        return getTemplate().isAutoInsertable();
    }

    @Override
    public StyledString getStyledDisplayString() {
        final StyledString label = new StyledString(getDisplayString());
        for (final Range<Integer> matchingRange : match) {
            final int length = Math.min(matchingRange.upperEndpoint() - matchingRange.lowerEndpoint(),
                    label.length() - matchingRange.lowerEndpoint());
            label.setStyle(matchingRange.lowerEndpoint(), length, Stylers.Common.MATCH_DECORATION_STYLER);
        }
        final int descPartLength = getTemplate().getDescription().length() + 2;
        label.setStyle(label.length() - descPartLength, descPartLength, Stylers.Common.ECLIPSE_DECORATION_STYLER);
        return label;
    }

}
