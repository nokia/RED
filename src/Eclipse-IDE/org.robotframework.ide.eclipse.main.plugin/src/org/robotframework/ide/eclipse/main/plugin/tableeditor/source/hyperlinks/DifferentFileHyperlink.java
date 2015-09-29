/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.hyperlinks;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;

/**
 * @author Michal Anglart
 *
 */
public class DifferentFileHyperlink implements IHyperlink {

    private final IRegion source;

    private final IRegion destination;

    private final IFile destinationFile;

    public DifferentFileHyperlink(final IRegion from, final IFile toFile, final IRegion to) {
        this.source = from;
        this.destinationFile = toFile;
        this.destination = to;
    }

    @Override
    public IRegion getHyperlinkRegion() {
        return source;
    }

    @Override
    public String getTypeLabel() {
        return null;
    }

    @Override
    public String getHyperlinkText() {
        return null;
    }

    @Override
    public void open() {
        final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IEditorDescriptor desc = editorRegistry.findEditor(RobotFormEditor.ID);
        try {
            final IEditorPart ed = page.openEditor(new FileEditorInput(destinationFile), desc.getId());
            if (ed instanceof RobotFormEditor) { // it can be ErrorEditorPart if something went wrong
                final RobotFormEditor editor = (RobotFormEditor) ed;
                final SuiteSourceEditor sourcePage = editor.activateSourcePage();

                sourcePage.getSelectionProvider()
                        .setSelection(new TextSelection(destination.getOffset(), destination.getLength()));
            }
        } catch (final PartInitException e) {
            throw new RuntimeException("Unable to open editor for file: " + destinationFile.getName(), e);
        }
    }
}
