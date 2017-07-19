/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposalAdapter;
import org.robotframework.red.jface.assist.RedContentProposalAdapter.RedContentProposalListener;
import org.robotframework.red.jface.assist.RedContentProposalProvider;

/**
 * @author Michal Anglart
 *
 */
public class AssistanceSupport {

    private final RedContentProposalProvider proposalsProvider;

    private RedContentProposalAdapter adapter;

    AssistanceSupport(final RedContentProposalProvider proposalsProvider) {
        this.proposalsProvider = proposalsProvider;
    }

    public void install(final Text text, final AssistantContext context) {
        install(text, context, getContentAssistActivationTrigger(), null);
    }

    public void install(final Text text, final AssistantContext context, final RedContentProposalListener listener) {
        install(text, context, getContentAssistActivationTrigger(), listener);
    }

    private void install(final Text text, final AssistantContext context, final KeySequence activationTrigger,
            final RedContentProposalListener listener) {
        if (proposalsProvider == null || text.isDisposed() || activationTrigger == null) {
            return;
        }
        adapter = listener == null
                ? RedContentProposalAdapter.install(text, context, proposalsProvider, activationTrigger)
                : RedContentProposalAdapter.install(text, context, proposalsProvider, activationTrigger, listener);
        RedContentProposalAdapter.markControlWithDecoration(adapter);
    }

    private KeySequence getContentAssistActivationTrigger() {
        final IBindingService service = PlatformUI.getWorkbench().getAdapter(IBindingService.class);
        return (KeySequence) service.getBestActiveBindingFor(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
    }

    public boolean areContentProposalsShown() {
        return adapter != null && adapter.isProposalPopupOpen();
    }

    public static class NatTableAssistantContext implements AssistantContext {

        private final int column;

        private final int row;

        public NatTableAssistantContext(final int column, final int row) {
            this.column = column;
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public int getRow() {
            return row;
        }
    }
}
