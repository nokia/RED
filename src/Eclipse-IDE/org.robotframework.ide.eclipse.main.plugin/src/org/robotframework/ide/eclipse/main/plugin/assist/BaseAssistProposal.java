/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;

import com.google.common.collect.Range;


abstract class BaseAssistProposal implements AssistProposal {

    protected final ProposalMatch match;

    protected String content;

    public BaseAssistProposal(final String content, final ProposalMatch match) {
        this.match = match;
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public List<String> getArguments() {
        return new ArrayList<>();
    }

    @Override
    public ImageDescriptor getImage() {
        return null;
    }

    @Override
    public String getLabel() {
        return content;
    }

    @Override
    public StyledString getStyledLabel() {
        final StyledString label = new StyledString(getLabel());
        for (final Range<Integer> matchingRange : match) {
            final int length = Math.min(matchingRange.upperEndpoint() - matchingRange.lowerEndpoint(),
                    label.length() - matchingRange.lowerEndpoint());
            label.setStyle(matchingRange.lowerEndpoint(), length, Stylers.Common.MARKED_PREFIX_STYLER);
        }
        return label;
    }

    @Override
    public boolean isDocumented() {
        return false;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public DocumentationViewInput getDocumentationInput() {
        return null;
    }
}
