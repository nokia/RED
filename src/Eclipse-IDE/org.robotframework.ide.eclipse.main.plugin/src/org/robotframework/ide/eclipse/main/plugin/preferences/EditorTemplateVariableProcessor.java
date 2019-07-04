/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.util.function.Supplier;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.TemplateContextType;

import com.google.common.collect.Streams;

public class EditorTemplateVariableProcessor implements IContentAssistProcessor {

    private final Supplier<TemplateContextType> contextTypeSupplier;

    EditorTemplateVariableProcessor(final Supplier<TemplateContextType> contextTypeSupplier) {
        this.contextTypeSupplier = contextTypeSupplier;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int documentOffset) {
        final TemplateContextType contextType = contextTypeSupplier.get();
        if (contextType == null) {
            return null;
        }

        final String text = viewer.getDocument().get();
        final int start = getStart(text, documentOffset);
        final int end = documentOffset;

        final String string = text.substring(start, end);
        final String prefix = string.length() >= 2 ? string.substring(2) : null;

        final int offset = start;
        final int length = end - start;

        return Streams.stream(contextType.resolvers())
                .filter(var -> prefix == null || var.getType().startsWith(prefix))
                .map(var -> new EditorTemplateVariableProposal(var, offset, length, viewer.getTextWidget()::getShell))
                .sorted((l, r) -> l.getDisplayString().compareTo(r.getDisplayString()))
                .toArray(ICompletionProposal[]::new);
    }

    private int getStart(final String string, final int end) {
        if (end >= 1 && string.charAt(end - 1) == '$') {
            return end - 1;
        }
        int start = end;
        while (start != 0 && Character.isUnicodeIdentifierPart(string.charAt(start - 1))) {
            start--;
        }
        if (start >= 2 && string.charAt(start - 1) == '{' && string.charAt(start - 2) == '$') {
            return start - 2;
        } else {
            return end;
        }
    }

    @Override
    public IContextInformation[] computeContextInformation(final ITextViewer viewer, final int documentOffset) {
        return null;
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[] { '$' };
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }
}
