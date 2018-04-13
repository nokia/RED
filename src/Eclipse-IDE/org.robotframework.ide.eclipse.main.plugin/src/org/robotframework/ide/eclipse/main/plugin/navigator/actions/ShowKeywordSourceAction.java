/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.SourceOpeningSupport;
import org.robotframework.red.viewers.Selections;

public class ShowKeywordSourceAction extends Action implements IEnablementUpdatingAction {

    private final IWorkbenchPage page;
    private final ISelectionProvider selectionProvider;

    public ShowKeywordSourceAction(final IWorkbenchPage page, final ISelectionProvider selectionProvider) {
        super("Show keyword source");

        this.page = page;
        this.selectionProvider = selectionProvider;
    }

    @Override
    public void updateEnablement(final IStructuredSelection selection) {
        setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof KeywordSpecification);
    }

    @Override
    public void run() {
        final ITreeSelection selection = (ITreeSelection) selectionProvider.getSelection();
        final LibrarySpecification libSpec = RedPlugin
                .getAdapter(selection.getPaths()[0].getParentPath().getLastSegment(), LibrarySpecification.class);
        final KeywordSpecification kwSpec = Selections.getSingleElement(selection, KeywordSpecification.class);
        final IProject project = RedPlugin.getAdapter(selection.getPaths()[0].getFirstSegment(), IProject.class);

        SourceOpeningSupport.open(page, RedPlugin.getModelManager().getModel(), project, libSpec, kwSpec);
    }

}
