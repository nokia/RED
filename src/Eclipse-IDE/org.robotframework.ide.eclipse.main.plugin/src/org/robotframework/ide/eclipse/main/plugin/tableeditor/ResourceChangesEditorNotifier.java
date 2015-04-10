package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;

class ResourceChangesEditorNotifier implements IResourceChangeListener {

    private final FormEditor editor;

    ResourceChangesEditorNotifier(final FormEditor editor) {
        this.editor = editor;
    }

    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        final FileEditorInput input = (FileEditorInput) editor.getEditorInput();
        final IFile file = input.getFile();

        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            try {
                event.getDelta().accept(new IResourceDeltaVisitor() {
                    @Override
                    public boolean visit(final IResourceDelta delta) {
                        if (delta.getKind() == IResourceDelta.REMOVED && file.equals(delta.getResource())) {
                            return !closeEditor();
                        }
                        return true;
                    }
                });
            } catch (final CoreException e) {
                throw new RuntimeException("Unable to detect resource change type", e);
            }
        } else if (event.getType() == IResourceChangeEvent.PRE_CLOSE
                || event.getType() == IResourceChangeEvent.PRE_DELETE) {
            if (file.getProject().equals(event.getResource())) {
                closeEditor();
            }
        }
    }

    private boolean closeEditor() {
        return editor.getSite().getPage().closeEditor(editor, true);
    }
}