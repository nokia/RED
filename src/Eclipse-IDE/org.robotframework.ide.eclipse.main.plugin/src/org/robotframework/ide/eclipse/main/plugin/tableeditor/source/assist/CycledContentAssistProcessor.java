/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionListenerExtension;
import org.eclipse.jface.text.contentassist.ICompletionListenerExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.swt.widgets.Display;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 */
public class CycledContentAssistProcessor extends DefaultContentAssistProcessor implements IContentAssistProcessor,
        ICompletionListener, ICompletionListenerExtension, ICompletionListenerExtension2 {

    private final SuiteSourceAssistantContext assistContext;

    private final AssistantCallbacks assistant;

    private final List<RedContentAssistProcessor> processors;

    private int currentPage;

    private boolean canReopenAssistantProgramatically;

    public CycledContentAssistProcessor(final SuiteSourceAssistantContext assistContext,
            final AssistantCallbacks assistant) {
        this.assistContext = assistContext;
        this.assistant = assistant;
        this.processors = new ArrayList<>();
        this.currentPage = 0;
        this.canReopenAssistantProgramatically = false;
    }

    @VisibleForTesting
    void setCanReopenAssistantProgramatically(final boolean canReopenAssistantProgramatically) {
        this.canReopenAssistantProgramatically = canReopenAssistantProgramatically;
    }

    public void addProcessor(final RedContentAssistProcessor processor) {
        processors.add(processor);
    }

    private RedContentAssistProcessor getCurrentProcessor() {
        return processors.get(currentPage);
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
        final RedContentAssistProcessor nextApplicableProcessor = getNextApplicableProcessor(viewer.getDocument(),
                offset);
        assistant.setStatus(nextApplicableProcessor == getCurrentProcessor() ? ""
                : String.format("Press %s to show %s proposals", assistContext.getActivationTrigger().format(),
                        nextApplicableProcessor.getProposalsTitle()));

        final ICompletionProposal[] proposals = getCurrentProcessor().computeCompletionProposals(viewer, offset);

        currentPage = processors.indexOf(nextApplicableProcessor);
        return proposals;
    }

    private RedContentAssistProcessor getNextApplicableProcessor(final IDocument document, final int offset) {
        int i = 1;
        while (!processorFromIndexIsApplicable((currentPage + i) % processors.size(), document, offset)) {
            i++;
            if (i > processors.size()) {
                return processors.get(0);
            }
        }
        return processors.get((currentPage + i) % processors.size());
    }

    private boolean processorFromIndexIsApplicable(final int index, final IDocument document, final int offset) {
        final RedContentAssistProcessor processor = processors.get(index);
        try {
            return processor.isInApplicableContentType(document, offset);
        } catch (final BadLocationException e) {
            throw new IllegalStateException("Offset should be always valid!", e);
        }
    }

    @Override
    public void assistSessionStarted(final ContentAssistEvent event) {
        if (event.processor == this) {
            assistContext.refreshPreferences();
            canReopenAssistantProgramatically = true;
            currentPage = 0;
        } else {
            canReopenAssistantProgramatically = false;
        }
    }

    @Override
    public void assistSessionRestarted(final ContentAssistEvent event) {
        if (event.processor == this) {
            canReopenAssistantProgramatically = true;
            currentPage--;
            if (currentPage < 0) {
                currentPage = processors.size() - 1;
            }
        } else {
            canReopenAssistantProgramatically = false;
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
        // this method is called also for processors from which the proposal was not chosen
        // hence canReopenAssistantProgramatically is holding information which proccessor
        // is able to open proposals after accepting
        if (!canReopenAssistantProgramatically) {
            return;
        }
        if (shouldActivateAssist(proposal)) {
            canReopenAssistantProgramatically = false;
            Display.getCurrent().asyncExec(() -> assistant.openCompletionProposals());
        }

        for (final Runnable operation : getOperationsAfterAccept(proposal)) {
            operation.run();
        }
    }

    private boolean shouldActivateAssist(final ICompletionProposal proposal) {
        return proposal instanceof RedCompletionProposal
                && ((RedCompletionProposal) proposal).shouldActivateAssistantAfterAccepting()
                || proposal instanceof RedCompletionProposalAdapter
                        && ((RedCompletionProposalAdapter) proposal).shouldActivateAssistantAfterAccepting();
    }

    private Collection<Runnable> getOperationsAfterAccept(final ICompletionProposal proposal) {
        if (proposal instanceof RedCompletionProposal) {
            return ((RedCompletionProposal) proposal).operationsToPerformAfterAccepting();
        } else if (proposal instanceof RedCompletionProposalAdapter) {
            return ((RedCompletionProposalAdapter) proposal).operationsToPerformAfterAccepting();
        }
        return new ArrayList<>();
    }

    @Override
    public void selectionChanged(final ICompletionProposal proposal, final boolean smartToggle) {
        // nothing to do here
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return assistContext.getAssistantAutoActivationChars();
    }

    public interface AssistantCallbacks {

        void setStatus(String title);

        void openCompletionProposals();
    }
}
