package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesEditorPage;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.VariablesEditorPage;

public class RobotFormEditor extends FormEditor {

    public static final String ID = "org.robotframework.ide.tableditor";

    private IResourceChangeListener resourceChangeListener;

    private boolean isReadOnly;

    @Override
    public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
        try {
            super.init(site, input);
            addResourceChangeListener();
        } catch (final IllegalRobotEditorInputException e) {
            throw new PartInitException("Unable to open editor", e);
        }
    }

    @Override
    protected void setInput(final IEditorInput input) {
        if (input instanceof FileEditorInput) {
            final IFile file = ((FileEditorInput) input).getFile();
            isReadOnly = file.isReadOnly();
            setPartName(input.getName());
        } else {
            final IStorage storage = (IStorage) input.getAdapter(IStorage.class);
            if (storage != null) {
                setPartName(storage.getName() + " [" + storage.getFullPath() + "]");
            } else {
                throw new IllegalRobotEditorInputException("Unable to open editor: unrecognized input of class: "
                        + input.getClass().getName());
            }
        }
        super.setInput(input);
    }

    private void addResourceChangeListener() {
        resourceChangeListener = new ResourceChangesEditorNotifier(this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener,
                IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE);
    }

    @Override
    protected void addPages() {
        try {
            addEditCasesPage();
            addVariablesPage();
        } catch (final PartInitException e) {
            throw new RuntimeException("Unable to initialize editor", e);
        }
    }

    private void addEditCasesPage() throws PartInitException {
        final FormPage main = new CasesEditorPage(this);
        final int index = addPage(main);
        setPageImage(index, RobotImages.getRobotImage().createImage());
    }

    private void addVariablesPage() throws PartInitException {
        final FormPage main = new VariablesEditorPage(this);
        final int index = addPage(main);
        setPageImage(index, RobotImages.getRobotVariableImage().createImage());
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        commitPages(true);
        for (final IEditorPart dirtyEditorPart : getDirtyEditors()) {
            dirtyEditorPart.doSave(monitor);
        }
    }

    @Override
    public boolean isSaveAsAllowed() {
        return getActiveEditor() != null && getActiveEditor().isSaveAsAllowed();
    }

    @Override
    public void doSaveAs() {
        commitPages(true);
        for (final IEditorPart dirtyEditorPart : getDirtyEditors()) {
            dirtyEditorPart.doSaveAs();
        }
    }

    private List<IEditorPart> getDirtyEditors() {
        final List<IEditorPart> list = new ArrayList<>();
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart editor = getEditor(i);
            if (editor.isDirty()) {
                list.add(editor);
            }
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (resourceChangeListener != null) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
        }
    }

    public RobotSuiteFile provideModel() {
        if (getEditorInput() instanceof FileEditorInput) {
            return RobotFramework.getModelManager().createSuiteFile(((FileEditorInput) getEditorInput()).getFile());
        }
        // final IStorage storage = (IStorage)
        // getEditorInput().getAdapter(IStorage.class);
        return null;
    }

    public IEditorPart activatePage(final RobotSuiteFileSection section) {
        int index = -1;

        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart part = (IEditorPart) pages.get(i);
            if (part instanceof RobotSectionPart && ((RobotSectionPart) part).isPartFor(section)) {
                index = i;
                break;
            }
        }
        if (index > 0) {
            setActivePage(index);
            return (IEditorPart) pages.get(index);
        }
        return null;
    }

    private static class IllegalRobotEditorInputException extends RuntimeException {

        public IllegalRobotEditorInputException(final String message) {
            super(message);
        }
    }
}
