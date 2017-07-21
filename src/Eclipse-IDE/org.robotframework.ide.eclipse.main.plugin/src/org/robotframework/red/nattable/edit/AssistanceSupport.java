/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import org.eclipse.swt.widgets.Text;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposalAdapter;
import org.robotframework.red.jface.assist.RedContentProposalAdapter.RedContentProposalListener;
import org.robotframework.red.jface.assist.RedContentProposalProvider;

/**
 * @author Michal Anglart
 */
public class AssistanceSupport {

    private final RedContentProposalProvider proposalsProvider;

    private RedContentProposalAdapter adapter;

    AssistanceSupport(final RedContentProposalProvider proposalsProvider) {
        this.proposalsProvider = proposalsProvider;
    }

    public void install(final Text text, final AssistantContext context) {
        install(text, context, null);
    }

    public void install(final Text text, final AssistantContext context, final RedContentProposalListener listener) {
        if (proposalsProvider == null || text.isDisposed()) {
            return;
        }
        adapter = listener == null ? RedContentProposalAdapter.install(text, context, proposalsProvider)
                : RedContentProposalAdapter.install(text, context, proposalsProvider, listener);
        if (adapter != null) {
            RedContentProposalAdapter.markControlWithDecoration(adapter);
        }
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
