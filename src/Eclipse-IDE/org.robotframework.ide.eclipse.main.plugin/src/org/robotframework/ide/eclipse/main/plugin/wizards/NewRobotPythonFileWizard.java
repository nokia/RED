/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * @author Michal Anglart
 *
 */
public class NewRobotPythonFileWizard extends BasicNewResourceWizard {

    private WizardNewRobotPythonFilePage mainPage;

    @Override
    public void init(final IWorkbench workbench, final IStructuredSelection currentSelection) {
        super.init(workbench, currentSelection);
        setNeedsProgressMonitor(true);
        setWindowTitle("New Robot python file");
    }

    @Override
    public void addPages() {
        super.addPages();

        mainPage = new WizardNewRobotPythonFilePage("New Robot python file", getSelection());
        mainPage.setWizard(this);
        mainPage.setTitle("Robot python file");
        mainPage.setDescription("Create new Robot python file (variable/library)");

        this.addPage(mainPage);
    }

    @Override
    public boolean performFinish() {
        final IFile newFile = mainPage.createNewFile();
        selectAndReveal(newFile);

        try {
            final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IEditorDescriptor desc = IDE.getEditorDescriptor(newFile);
            if (!desc.isInternal()) {
                // we don't want to open .py file with interpreter, so if there
                // is no internal editor, then we will use default text editor
                final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
                desc = editorRegistry.findEditor("org.eclipse.ui.DefaultTextEditor");
                if (desc == null) {
                    throw new EditorOpeningException("Unable to open editor for file: " + newFile.getName());
                } else {
                    page.openEditor(new FileEditorInput(newFile), desc.getId());
                }
            }
        } catch (final PartInitException e) {
            throw new EditorOpeningException("Unable to open editor for file: " + newFile.getName(), e);
        }

        return true;
    }

    private static class EditorOpeningException extends RuntimeException {

        public EditorOpeningException(final String msg) {
            super(msg);
        }

        public EditorOpeningException(final String msg, final PartInitException cause) {
            super(msg, cause);
        }
    }

}
