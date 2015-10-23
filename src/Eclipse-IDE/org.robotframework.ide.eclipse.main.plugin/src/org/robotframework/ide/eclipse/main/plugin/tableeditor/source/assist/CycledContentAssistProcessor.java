/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionListenerExtension;
import org.eclipse.jface.text.contentassist.ICompletionListenerExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.RedCompletionProposal;

/**
 * @author Michal Anglart
 *
 */
public class CycledContentAssistProcessor extends DefaultContentAssistProcessor implements IContentAssistProcessor,
        ICompletionListener, ICompletionListenerExtension, ICompletionListenerExtension2 {

    private final List<RedContentAssistProcessor> processors;

    private final AssitantCallbacks assistant;

    private int currentPage;

    public CycledContentAssistProcessor(final AssitantCallbacks assistant) {
        this.processors = newArrayList();
        this.assistant = assistant;
        this.currentPage = 0;
    }

    public void addProcessor(final RedContentAssistProcessor processor) {
        processors.add(processor);
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
        final String title = processors.get((currentPage + 1) % processors.size()).getProposalsTitle();
        assistant.setStatus(title);

        final ICompletionProposal[] proposals = processors.get(currentPage).computeCompletionProposals(viewer, offset);

        currentPage = (currentPage + 1) % processors.size();
        return proposals;
    }

    @Override
    public void assistSessionStarted(final ContentAssistEvent event) {
        if (event.processor == this) {
            currentPage = 0;
        }
    }

    @Override
    public void assistSessionRestarted(final ContentAssistEvent event) {
        if (event.processor == this) {
            currentPage--;
            if (currentPage < 0) {
                currentPage = processors.size() - 1;
            }
        }

    }

    @Override
    public void assistSessionEnded(final ContentAssistEvent event) {
        if (event.processor == this) {
            currentPage = 0;
        }
    }

    @Override
    public void applied(final ICompletionProposal proposal) {
        if (proposal instanceof RedCompletionProposal
                && ((RedCompletionProposal) proposal).shouldActivateAssitantAfterAccepting()) {
            assistant.openCompletionProposals();
        }
    }

    @Override
    public void selectionChanged(final ICompletionProposal proposal, final boolean smartToggle) {
        // nothing to do here
    }

    public interface AssitantCallbacks {

        void setStatus(String title);

        void openCompletionProposals();
    }
}
