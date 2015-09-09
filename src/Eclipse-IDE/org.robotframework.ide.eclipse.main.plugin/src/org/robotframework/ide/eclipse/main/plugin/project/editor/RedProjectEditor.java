/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigWriter;

public class RedProjectEditor extends MultiPageEditorPart {

    public static final String ID = "org.robotframework.ide.project.editor";

    private RedProjectEditorInput redProjectEditorInput;

    private IEclipseContext context;

    private RedProjectConfigurationEditorPart projectConfigPart;

    @Override
    protected void setInput(final IEditorInput input) {
        if (input instanceof FileEditorInput) {
            final IFile file = ((FileEditorInput) input).getFile();
            setPartName(file.getProject().getName() + "/" + input.getName());

            redProjectEditorInput = new RedProjectEditorInput(!file.isReadOnly(),
                    new RobotProjectConfigReader().readConfiguration(file), file.getProject());
        } else {
            final IStorage storage = (IStorage) input.getAdapter(IStorage.class);
            if (storage != null) {
                setPartName(storage.getName() + " [" + storage.getFullPath() + "]");

                try {
                    redProjectEditorInput = new RedProjectEditorInput(!storage.isReadOnly(), new RobotProjectConfigReader().readConfiguration(storage.getContents()), null);
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
    protected void createPages() {
        try {
            context = ((IEclipseContext) getEditorSite().getService(IEclipseContext.class)).getActiveLeaf();
            context.set(RedProjectEditorInput.class, redProjectEditorInput);
            context.set(IEditorSite.class, getEditorSite());
            context.set(RedProjectEditor.class, this);
            projectConfigPart = new RedProjectConfigurationEditorPart();
            ContextInjectionFactory.inject(projectConfigPart, context);

            final int index = addPage(projectConfigPart, getEditorInput());
            setPageText(index, "RED Project");
        } catch (final PartInitException e) {
            throw new RuntimeException("Unable to initialize editor", e);
        }
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        for (final IEditorPart dirtyEditor : getDirtyEditors()) {
            dirtyEditor.doSave(monitor);
        }
        final RobotProject project = redProjectEditorInput.getRobotProject();
        project.clear();
        new RobotProjectConfigWriter().writeConfiguration(redProjectEditorInput.getProjectConfiguration(), project);
    }

    private List<? extends IEditorPart> getDirtyEditors() {
        final List<IEditorPart> dirtyEditors = newArrayList();
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart editorPart = getEditor(i);
            if (editorPart != null && editorPart.isDirty()) {
                dirtyEditors.add(editorPart);
            }
        }
        return dirtyEditors;
    }

    @Override
    public void doSaveAs() {
        // TODO : implement if needed
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void dispose() {
        super.dispose();
        ContextInjectionFactory.uninject(projectConfigPart, context);
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
