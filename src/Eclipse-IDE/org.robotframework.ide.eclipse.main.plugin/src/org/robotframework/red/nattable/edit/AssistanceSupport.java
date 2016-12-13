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

import com.google.common.base.Optional;

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

    public void install(final Text textControl, final AssistantContext context,
            final Optional<RedContentProposalListener> listener) {
        if (proposalsProvider == null || textControl.isDisposed()) {
            return;
        }
        adapter = RedContentProposalAdapter.install(textControl, context, proposalsProvider, listener);
        RedContentProposalAdapter.markControlWithDecoration(adapter);
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
