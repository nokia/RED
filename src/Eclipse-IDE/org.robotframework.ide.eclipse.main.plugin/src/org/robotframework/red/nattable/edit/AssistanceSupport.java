/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.IContentProposingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Text;
import org.robotframework.red.jface.assist.RedContentProposalAdapter;
import org.robotframework.red.jface.assist.RedContentProposalAdapter.RedContentProposalListener;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
public class AssistanceSupport {

    private final IContentProposingSupport support;

    private RedContentProposalAdapter adapter;


    AssistanceSupport(final IContentProposingSupport support) {
        this.support = support;
    }

    public void install(final Text textControl, final Optional<RedContentProposalListener> listener,
            final int acceptanceStyle) {
        if (support == null || textControl.isDisposed()) {
            return;
        }

        adapter = new RedContentProposalAdapter(textControl, support.getControlAdapter(textControl),
                support.getProposalProvider(), support.getKeyStroke(), support.getActivationKeys());
        adapter.setProposalAcceptanceStyle(acceptanceStyle);
        adapter.setLabelProvider(support.getLabelProvider());
        adapter.setAutoActivationDelay(200);
        if (listener.isPresent()) {
            adapter.addContentProposalListener(listener.get());
        }

        final ControlDecoration decoration = new ControlDecoration(textControl, SWT.RIGHT | SWT.TOP);
        decoration.setDescriptionText("Press Ctrl+Space for content assist");
        decoration.setImage(FieldDecorationRegistry.getDefault()
                .getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL)
                .getImage());
        textControl.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(final DisposeEvent e) {
                decoration.dispose();
            }
        });
    }

    public boolean areContentProposalsShown() {
        return adapter != null && adapter.isProposalPopupOpen();
    }
}
