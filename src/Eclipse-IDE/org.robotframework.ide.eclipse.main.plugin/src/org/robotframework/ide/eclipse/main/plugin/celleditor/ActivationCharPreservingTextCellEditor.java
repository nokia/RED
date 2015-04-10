package org.robotframework.ide.eclipse.main.plugin.celleditor;

import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationListener;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * This is a custom implementation of TextCellEditor which can be activated by
 * any character key press and this character will be preserved after editor
 * activation.
 * 
 */
public class ActivationCharPreservingTextCellEditor extends TextCellEditor {

    private final String prefix;
    private final String suffix;

    public ActivationCharPreservingTextCellEditor(final ColumnViewerEditor viewerEditor, final Composite parent,
            final String prefix, final String suffix) {
        super(parent, SWT.SINGLE);
        this.prefix = prefix;
        this.suffix = suffix;

        registerActivationListener(viewerEditor);
    }

    public ActivationCharPreservingTextCellEditor(final ColumnViewerEditor viewerEditor, final Composite parent) {
        this(viewerEditor, parent, "", "");
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
    public void activate(final ColumnViewerEditorActivationEvent activationEvent) {
        super.activate(activationEvent);
        if (activationEvent.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
                && activationEvent.character != SWT.CR) {
            text.setText(prefix + Character.toString(activationEvent.character) + suffix);
        }
    }

    private class EditorActivationListener extends ColumnViewerEditorActivationListener {

        @Override
        public void beforeEditorDeactivated(final ColumnViewerEditorDeactivationEvent event) {
            // nothing to do
        }

        @Override
        public void beforeEditorActivated(final ColumnViewerEditorActivationEvent event) {
            // nothing to do
        }

        @Override
        public void afterEditorDeactivated(final ColumnViewerEditorDeactivationEvent event) {
            // nothing to do
        }

        @Override
        public void afterEditorActivated(final ColumnViewerEditorActivationEvent event) {
            final Text text = (Text) ActivationCharPreservingTextCellEditor.this.getControl();
            text.setSelection(prefix.length(), text.getText().length() - suffix.length());
        }
    };
}
