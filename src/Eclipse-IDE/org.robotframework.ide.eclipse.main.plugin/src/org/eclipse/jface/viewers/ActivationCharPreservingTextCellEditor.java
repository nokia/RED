/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.eclipse.jface.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.robotframework.red.jface.assist.RedContentProposalAdapter;

/**
 * This is a custom implementation of TextCellEditor which can be activated by
 * any character key press and this character will be preserved after editor
 * activation.
 * 
 */
public class ActivationCharPreservingTextCellEditor extends TextCellEditor {

    private final String contextToDeactivate;
    private RedContentProposalAdapter contentProposalAdapter;

    public ActivationCharPreservingTextCellEditor(final ColumnViewerEditor viewerEditor, final Composite parent,
            final String contextToDeactivate) {
        super(parent, SWT.SINGLE);
        this.contextToDeactivate = contextToDeactivate;
        registerActivationListener(viewerEditor);
    }

    private void registerActivationListener(final ColumnViewerEditor viewerEditor) {
        final EditorActivationListener activationListener = new EditorActivationListener();
        viewerEditor.addEditorActivationListener(activationListener);
        getControl().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent e) {
                viewerEditor.removeEditorActivationListener(activationListener);
            }
        });
    }

    @Override
    protected boolean dependsOnExternalFocusListener() {
        return false;
    }

    @Override
    protected void focusLost() {
        if (isActivated() && (contentProposalAdapter == null || !isContentProposalOpened())) {
            fireApplyEditorValue();
            deactivate();
        }
    }

    protected final boolean isContentProposalOpened() {
        return contentProposalAdapter != null && contentProposalAdapter.isProposalPopupOpen();
    }

    @Override
    public void activate(final ColumnViewerEditorActivationEvent activationEvent) {
        super.activate(activationEvent);
        if (activationEvent.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
                && activationEvent.character != SWT.CR) {
            text.setText(Character.toString(activationEvent.character));
        }
    }

    public void addContentProposalsSupport(final IContentProposingSupport support) {
        final ILabelProvider labelProvider = support.getLabelProvider();
        contentProposalAdapter = new RedContentProposalAdapter(text, support.getControlAdapter(text),
                support.getProposalProvider(), support.getKeyStroke(), support.getActivationKeys());
        contentProposalAdapter.setLabelProvider(labelProvider);
        contentProposalAdapter.setAutoActivationDelay(200);
        contentProposalAdapter.setProposalAcceptanceStyle(RedContentProposalAdapter.PROPOSAL_SHOULD_REPLACE);
    }

    @Override
    public void dispose() {
        contentProposalAdapter = null;
        super.dispose();
    }

    private class EditorActivationListener extends ColumnViewerEditorActivationListener {

        private IContextActivation contextActivation;

        @Override
        public void beforeEditorDeactivated(final ColumnViewerEditorDeactivationEvent event) {
            final IContextService service = PlatformUI.getWorkbench()
                    .getService(
                    IContextService.class);
            service.deactivateContext(contextActivation);
        }

        @Override
        public void beforeEditorActivated(final ColumnViewerEditorActivationEvent event) {
            final IContextService service = PlatformUI.getWorkbench()
                    .getService(
                    IContextService.class);
            contextActivation = service.activateContext(contextToDeactivate);
        }

        @Override
        public void afterEditorActivated(final ColumnViewerEditorActivationEvent event) {
            final Text text = (Text) ActivationCharPreservingTextCellEditor.this.getControl();
            final int end = text.getText().length();
            text.setSelection(end, end);
        }

        @Override
        public void afterEditorDeactivated(final ColumnViewerEditorDeactivationEvent event) {
            // nothing to do
        }
    };
}
