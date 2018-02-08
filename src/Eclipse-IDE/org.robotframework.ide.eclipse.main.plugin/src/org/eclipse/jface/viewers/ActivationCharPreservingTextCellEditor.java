/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.eclipse.jface.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

/**
 * This is a custom implementation of TextCellEditor which can be activated by
 * any character key press and this character will be preserved after editor
 * activation.
 *
 */
public class ActivationCharPreservingTextCellEditor extends TextCellEditor {

    private final String contextToDeactivate;

    public ActivationCharPreservingTextCellEditor(final ColumnViewerEditor viewerEditor, final Composite parent,
            final String contextToDeactivate) {
        super(parent, SWT.SINGLE);
        this.contextToDeactivate = contextToDeactivate;
        registerActivationListener(viewerEditor);
    }

    private void registerActivationListener(final ColumnViewerEditor viewerEditor) {
        final EditorActivationListener activationListener = new EditorActivationListener();
        viewerEditor.addEditorActivationListener(activationListener);
        getControl().addDisposeListener(e -> viewerEditor.removeEditorActivationListener(activationListener));
    }

    @Override
    protected boolean dependsOnExternalFocusListener() {
        return false;
    }

    @Override
    protected void focusLost() {
        if (isActivated()) {
            fireApplyEditorValue();
            deactivate();
        }
    }

    @Override
    public void activate(final ColumnViewerEditorActivationEvent activationEvent) {
        super.activate(activationEvent);
        if (activationEvent.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
                && activationEvent.character != SWT.CR) {
            text.setText(Character.toString(activationEvent.character));
        }
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
    }
}
