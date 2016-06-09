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

class VariableContentProposal implements IRedContentProposal {

    private final RedVariableProposal wrappedProposal;

    private final String matchingPrefix;

    VariableContentProposal(final RedVariableProposal proposedVariable, final String matchingPrefix) {
        this.wrappedProposal = proposedVariable;
        this.matchingPrefix = matchingPrefix;
    }

    @Override
    public String getContent() {
        return wrappedProposal.getName();
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
        return wrappedProposal.getName();
    }

    @Override
    public String getLabelDecoration() {
        return "";
    }

    @Override
    public boolean hasDescription() {
        return true;
    }

    @Override
    public String getMatchingPrefix() {
        return matchingPrefix;
    }

    @Override
    public String getDescription() {
        final String sourceLabel = Strings.padEnd("Source:", 15, ' ');
        final String valueLabel = Strings.padEnd("Value:", 15, ' ');

        final Escaper escaper = XmlEscapers.xmlAttributeEscaper();

        final StringBuilder builder = new StringBuilder();
        builder.append("<form>");
        builder.append("<p><span font=\"monospace\">" + sourceLabel + escaper.escape(wrappedProposal.getSource())
                + "</span></p>");
        builder.append("<p><span font=\"monospace\">" + valueLabel + escaper.escape(wrappedProposal.getValue())
                + "</span></p>");
        builder.append("</form>");

        return builder.toString();
    }
}
