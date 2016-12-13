/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;

public class RedCompletionProposalAdapter implements Comparable<RedCompletionProposalAdapter>, ICompletionProposal,
        ICompletionProposalExtension3, ICompletionProposalExtension6 {

    private final AssistProposal adaptedProposal;

    private final DocumentationModification modification;

    private final Optional<IContextInformation> contextInformation;

    public RedCompletionProposalAdapter(final AssistProposal proposal, final DocumentationModification modification) {
        this(proposal, modification, null);
    }

    public RedCompletionProposalAdapter(final AssistProposal proposal, final DocumentationModification modification,
            final IContextInformation contextInformation) {
        this.adaptedProposal = proposal;
        this.modification = modification;
        this.contextInformation = Optional.fromNullable(contextInformation);
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
            return modification.toSelect;
        }
        final int x = modification.toReplace.offset + adaptedProposal.getContent().length()
                + modification.contentSuffix.length();
        return new Point(x, 0);
    }

    @Override
    public String getAdditionalProposalInfo() {
        if (adaptedProposal.hasDescription()) {
            return adaptedProposal.getDescription();
        }
        return null;
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
        return new IInformationControlCreator() {

            @Override
            public IInformationControl createInformationControl(final Shell parent) {
                return new DefaultInformationControl(parent);
            }
        };
    }

    @Override
    public IContextInformation getContextInformation() {
        return contextInformation.orNull();
    }

    @Override
    public int compareTo(final RedCompletionProposalAdapter that) {
        return this.getDisplayString().compareTo(that.getDisplayString());
    }

    public boolean shouldActivateAssitantAfterAccepting() {
        return modification.activateAssistant;
    }

    public Collection<Runnable> operationsToPerformAfterAccepting() {
        return modification.operationsAfterAccepting;
    }

    static class DocumentationModification {

        private final String contentSuffix;

        private final Position toReplace;

        public boolean activateAssistant;

        private final Point toSelect;

        public Collection<Runnable> operationsAfterAccepting;

        public DocumentationModification(final String contentSuffix, final Position toReplace) {
            this(contentSuffix, toReplace, null, false, new ArrayList<Runnable>());
        }

        public DocumentationModification(final String contentSuffix, final Position toReplace,
                final boolean shouldActivate) {
            this(contentSuffix, toReplace, null, shouldActivate, new ArrayList<Runnable>());
        }

        public DocumentationModification(final String contentSuffix, final Position toReplace, final Point toSelect,
                final Collection<Runnable> operationsAfterAccepting) {
            this(contentSuffix, toReplace, toSelect, false, operationsAfterAccepting);
        }

        public DocumentationModification(final String contentSuffix, final Position toReplace,
                final Collection<Runnable> operationsAfterAccepting) {
            this(contentSuffix, toReplace, null, false, operationsAfterAccepting);
        }

        public DocumentationModification(final String contentSuffix, final Position toReplace, final Point toSelect,
                final boolean activateAssistant, final Collection<Runnable> operationsAfterAccepting) {
            this.contentSuffix = contentSuffix;
            this.toReplace = toReplace;
            this.toSelect = toSelect;
            this.activateAssistant = activateAssistant;
            this.operationsAfterAccepting = operationsAfterAccepting;
        }
    }
}
