/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;


/**
 * @author Michal Anglart
 *
 */
public class FileHyperlink implements IHyperlink {

    private final IRegion source;

    private final IFile destinationFile;

    private final String label;

    public FileHyperlink(final IRegion from, final IFile toFile, final String label) {
        this.source = from;
        this.destinationFile = toFile;
        this.label = label;
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
        return label;
    }

    @Override
    public void open() {
        try {
            final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IEditorDescriptor desc = IDE.getEditorDescriptor(destinationFile);
            if (!desc.isInternal()) {
                // we don't want to open files with external editors (e.g. running script files etc)
                final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
                desc = editorRegistry.findEditor(EditorsUI.DEFAULT_TEXT_EDITOR_ID);
            }
            if (desc != null) {
                page.openEditor(new FileEditorInput(destinationFile), desc.getId());
            }
        } catch (final PartInitException e) {
            // nothing to do in such case
        }
    }

}
