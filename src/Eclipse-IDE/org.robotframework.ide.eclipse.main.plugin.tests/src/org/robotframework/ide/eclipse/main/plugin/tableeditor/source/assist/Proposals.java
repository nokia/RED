/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import java.util.Objects;

import org.assertj.core.api.Condition;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;


/**
 * @author Michal Anglart
 */
class Proposals {

    static Condition<? super ICompletionProposal> activatingAssistantAfterAccept() {
        return new Condition<ICompletionProposal>() {

            @Override
            public boolean matches(final ICompletionProposal proposal) {
                return ((RedCompletionProposalAdapter) proposal).shouldActivateAssistantAfterAccepting();
            }
        };
    }

    static Condition<? super ICompletionProposal> proposalWithImage(final Image image) {
        return new Condition<ICompletionProposal>() {

            @Override
            public boolean matches(final ICompletionProposal proposal) {
                return Objects.equals(proposal.getImage(), image);
            }
        };
    }

    static Condition<? super ICompletionProposal> proposalWithOperationsToPerformAfterAccepting(final int size) {
        return new Condition<ICompletionProposal>() {

            @Override
            public boolean matches(final ICompletionProposal proposal) {
                return ((RedCompletionProposalAdapter) proposal).operationsToPerformAfterAccepting().size() == size;
            }
        };
    }

    static IDocument applyToDocument(final IDocument document, final ICompletionProposal proposal) {
        final Document docCopy = new Document(document);
        proposal.apply(docCopy);
        return docCopy;
    }

    static IDocument applyToDocument(final ITextViewer viewer, final int offset, final TemplateProposal proposal) {
        proposal.apply(viewer, (char) -1, -1, offset);
        return new Document(viewer.getDocument());
    }

}
