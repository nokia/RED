/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.InformationControlSupport;
import org.robotframework.red.graphics.ImagesManager;

public class RedCompletionProposalAdapter implements Comparable<RedCompletionProposalAdapter>, ICompletionProposal,
        ICompletionProposalExtension3, ICompletionProposalExtension6 {

    private final SuiteSourceAssistantContext context;

    private final AssistProposal adaptedProposal;

    private final DocumentModification modification;

    private final IContextInformation contextInformation;


    public RedCompletionProposalAdapter(final SuiteSourceAssistantContext context, final AssistProposal proposal,
            final DocumentModification modification) {
        this(context, proposal, modification, null);
    }

    public RedCompletionProposalAdapter(final SuiteSourceAssistantContext context, final AssistProposal proposal,
            final DocumentModification modification, final IContextInformation contextInformation) {
        this.context = context;
        this.adaptedProposal = proposal;
        this.modification = modification;
        this.contextInformation = contextInformation;
    }

    @Override
    public StyledString getStyledDisplayString() {
        return adaptedProposal.getStyledLabel();
    }

    @Override
    public CharSequence getPrefixCompletionText(final IDocument document, final int completionOffset) {
        return adaptedProposal.getContent() + modification.contentSuffix;
    }

    @Override
    public int getPrefixCompletionStart(final IDocument document, final int completionOffset) {
        return modification.toReplace.getOffset();
    }

    @Override
    public void apply(final IDocument document) {
        try {
            document.replace(modification.toReplace.getOffset(), modification.toReplace.getLength(),
                    adaptedProposal.getContent() + modification.contentSuffix);
        } catch (final BadLocationException x) {
            // ignore
        }
    }

    @Override
    public Point getSelection(final IDocument document) {
        if (modification.toSelect != null) {
            return new Point(modification.toSelect.getOffset(), modification.toSelect.getLength());
        }
        final int x = modification.toReplace.offset + adaptedProposal.getContent().length()
                + modification.contentSuffix.length();
        return new Point(x, 0);
    }

    @Override
    public String getDisplayString() {
        return getStyledDisplayString().getString();
    }

    @Override
    public Image getImage() {
        return ImagesManager.getImage(adaptedProposal.getImage());
    }

    @Override
    public IInformationControlCreator getInformationControlCreator() {
        final InformationControlSupport infoControlSupport = context.getInfoControlSupport();
        return infoControlSupport.getHoverControlCreator();
    }

    @Override
    public String getAdditionalProposalInfo() {
        if (!adaptedProposal.isDocumented()) {
            return null;
        }
        
        final InformationControlSupport infoControlSupport = context.getInfoControlSupport();
        if (infoControlSupport.isBrowserBased()) {
            return adaptedProposal.getDocumentationInput().provideHtml(context.getEnvironment());
        }
        return adaptedProposal.getDescription();
    }

    @Override
    public IContextInformation getContextInformation() {
        return contextInformation;
    }

    @Override
    public int compareTo(final RedCompletionProposalAdapter that) {
        return this.getDisplayString().compareTo(that.getDisplayString());
    }

    public boolean shouldActivateAssistantAfterAccepting() {
        return modification.activateAssistant;
    }

    public Collection<Runnable> operationsToPerformAfterAccepting() {
        return modification.operationsAfterAccepting.get();
    }

    static class DocumentModification {

        private final String contentSuffix;

        private final Position toReplace;

        public boolean activateAssistant;

        private final Position toSelect;

        // calculating operations to perform after accepting may be time consuming, so instead of
        // precomputing this information for each proposal we're pushing the calculation into this
        // lambda, which is calculated only when proposal is chosen
        public Supplier<Collection<Runnable>> operationsAfterAccepting;

        public DocumentModification(final String contentSuffix, final Position toReplace) {
            this(contentSuffix, toReplace, null, false, () -> new ArrayList<>());
        }

        public DocumentModification(final String contentSuffix, final Position toReplace,
                final boolean shouldActivate) {
            this(contentSuffix, toReplace, null, shouldActivate, () -> new ArrayList<>());
        }

        public DocumentModification(final String contentSuffix, final Position toReplace, final Position toSelect) {
            this(contentSuffix, toReplace, toSelect, false, () -> new ArrayList<>());
        }

        public DocumentModification(final String contentSuffix, final Position toReplace, final Position toSelect,
                final Supplier<Collection<Runnable>> operationsAfterAccepting) {
            this(contentSuffix, toReplace, toSelect, false, operationsAfterAccepting);
        }

        public DocumentModification(final String contentSuffix, final Position toReplace,
                final Supplier<Collection<Runnable>> operationsAfterAccepting) {
            this(contentSuffix, toReplace, null, false, operationsAfterAccepting);
        }

        public DocumentModification(final String contentSuffix, final Position toReplace, final Position toSelect,
                final boolean activateAssistant, final Supplier<Collection<Runnable>> operationsAfterAccepting) {
            this.contentSuffix = contentSuffix;
            this.toReplace = toReplace;
            this.toSelect = toSelect;
            this.activateAssistant = activateAssistant;
            this.operationsAfterAccepting = operationsAfterAccepting;
        }
    }
}
