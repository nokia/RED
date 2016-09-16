/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.red.jface.assist.IRedContentProposal;

import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.xml.XmlEscapers;

class KeywordContentProposal implements IRedContentProposal {

    private final RedKeywordProposal wrappedProposal;
    private final String matchingPrefix;

    KeywordContentProposal(final RedKeywordProposal proposedKeyword, final String matchingPrefix) {
        this.wrappedProposal = proposedKeyword;
        this.matchingPrefix = matchingPrefix;
    }

    @Override
    public String getContent() {
        return wrappedProposal.getContent();
    }

    @Override
    public int getCursorPosition() {
        return getContent().length();
    }

    ImageDescriptor getImage() {
        return wrappedProposal.getImage();
    }

    @Override
    public String getLabel() {
        return wrappedProposal.getLabel();
    }

    @Override
    public String getLabelDecoration() {
        return wrappedProposal.getLabelDecoration();
    }

    @Override
    public boolean hasDescription() {
        return wrappedProposal.hasDescription();
    }

    @Override
    public String getDescription() {
        final String nameLabel = Strings.padEnd("Name:", 15, ' ');
        final String sourceLabel = Strings.padEnd("Source:", 15, ' ');
        final String argsLabel = Strings.padEnd("Arguments:", 15, ' ');

        final Escaper escaper = XmlEscapers.xmlAttributeEscaper();

        final StringBuilder builder = new StringBuilder();
        builder.append("<form>");
        builder.append("<p><span font=\"monospace\">" + nameLabel + escaper.escape(wrappedProposal.getLabel())
                + "</span></p>");
        builder.append("<p><span font=\"monospace\">" + sourceLabel + escaper.escape(wrappedProposal.getSourceName())
                + "</span></p>");
        builder.append("<p><span font=\"monospace\">" + argsLabel + escaper.escape(wrappedProposal.getArgumentsLabel())
                + "</span></p>");
        builder.append("<p></p>");
        builder.append(wrappedProposal.getHtmlDocumentation());
        builder.append("</form>");

        return builder.toString();
    }

    @Override
    public String getMatchingPrefix() {
        return matchingPrefix;
    }
}
