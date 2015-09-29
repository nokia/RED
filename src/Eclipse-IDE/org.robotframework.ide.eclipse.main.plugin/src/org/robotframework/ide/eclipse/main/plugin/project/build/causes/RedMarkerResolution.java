/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;

/**
 * @author Michal Anglart
 *
 */
public abstract class RedMarkerResolution implements IMarkerResolution {

    @Override
    public final void run(final IMarker marker) {
        final IFile file = (IFile) marker.getResource();

        final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
        final IEditorDescriptor desc = editorRegistry.findEditor(RobotFormEditor.ID);
        try {
            final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            final IEditorPart ed = page.openEditor(new FileEditorInput(file), desc.getId());
            if (ed instanceof RobotFormEditor) { // it can be ErrorEditorPart if something went
                                                 // wrong
                final RobotFormEditor editor = (RobotFormEditor) ed;
                final SuiteSourceEditor activatedPage = editor.activateSourcePage();
                activatedPage.setFocus();

                final IDocument document = activatedPage.getDocument();
                final RobotSuiteFile model = editor.provideSuiteModel();

                final ICompletionProposal asContentProposal = asContentProposal(marker, document, model);
                asContentProposal.apply(document);

                marker.delete();
            }
        } catch (final CoreException e) {
            // oh well, we won't fix this...
        }
    }

    public abstract ICompletionProposal asContentProposal(IMarker marker, IDocument document,
            RobotSuiteFile suiteModel);

}
