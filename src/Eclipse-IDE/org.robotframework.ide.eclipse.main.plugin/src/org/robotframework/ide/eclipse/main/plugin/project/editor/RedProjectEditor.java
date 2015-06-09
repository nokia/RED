package org.robotframework.ide.eclipse.main.plugin.project.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigWriter;

public class RedProjectEditor extends FormEditor {

    public static final String ID = "org.robotframework.ide.project.editor";

    private boolean isEditable;

    private RobotProjectConfig configuration;

    private IProject project;

    @Override
    protected void addPages() {
        try {
            addPage(new ProjectConfigurationFormPage(this, project, configuration, isEditable));
        } catch (final PartInitException e) {
            throw new RuntimeException("Unable to initialize editor", e);
        }
    }

    private void addPage(final FormPage page) throws PartInitException {
        final int index = addPage(page, getEditorInput());
        setPageImage(index, page.getTitleImage());
        setPageText(index, page.getPartName());
    }

    @Override
    protected void setInput(final IEditorInput input) {
        if (input instanceof FileEditorInput) {
            final IFile file = ((FileEditorInput) input).getFile();
            isEditable = !file.isReadOnly();
            setPartName(file.getProject().getName() + "/" + input.getName());

            configuration = new RobotProjectConfigReader().readConfiguration(file);
            project = file.getProject();
        } else {
            final IStorage storage = (IStorage) input.getAdapter(IStorage.class);
            if (storage != null) {
                isEditable = !storage.isReadOnly();
                setPartName(storage.getName() + " [" + storage.getFullPath() + "]");

                try {
                    configuration = new RobotProjectConfigReader().readConfiguration(storage.getContents());
                    project = null;
                } catch (final CoreException e) {
                    throw new IllegalProjectConfigurationEditorInputException(
                            "Unable to open editor: unrecognized input of class: " + input.getClass().getName(), e);
                }
            } else {
                throw new IllegalProjectConfigurationEditorInputException(
                        "Unable to open editor: unrecognized input of class: " + input.getClass().getName());
            }
        }
        super.setInput(input);
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        RobotFramework.getModelManager().getModel().createRobotProject(project).clear();
        new RobotProjectConfigWriter().writeConfiguration(configuration, project);

        commitPages(true);
        firePropertyChange(PROP_DIRTY);
    }

    @Override
    public void doSaveAs() {
        // FIXME : implement
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    private static class IllegalProjectConfigurationEditorInputException extends RuntimeException {
        public IllegalProjectConfigurationEditorInputException(final String message) {
            super(message);
        }

        public IllegalProjectConfigurationEditorInputException(final String message, final CoreException cause) {
            super(message, cause);
        }
    }
}
