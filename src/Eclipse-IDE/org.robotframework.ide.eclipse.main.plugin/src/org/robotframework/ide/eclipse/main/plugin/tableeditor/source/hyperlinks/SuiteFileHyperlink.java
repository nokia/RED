/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.hyperlinks;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor.RobotEditorOpeningException;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;

/**
 * @author Michal Anglart
 *
 */
public class SuiteFileHyperlink implements RedHyperlink {

    static final String DEFAULT_TEXT = "Open Definition";

    private final IRegion source;

    private final IRegion destination;

    private final RobotSuiteFile destinationFile;

    private final String label;

    public SuiteFileHyperlink(final IRegion from, final RobotSuiteFile toFile) {
        this(from, toFile, null, DEFAULT_TEXT);
    }

    public SuiteFileHyperlink(final IRegion from, final RobotSuiteFile toFile, final String label) {
        this(from, toFile, null, label);
    }

    public SuiteFileHyperlink(final IRegion from, final RobotSuiteFile toFile, final IRegion to) {
        this(from, toFile, to, DEFAULT_TEXT);
    }

    public SuiteFileHyperlink(final IRegion from, final RobotSuiteFile toFile, final IRegion to,
            final String label) {
        this.source = from;
        this.destinationFile = toFile;
        this.destination = to;
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
    public String getLabelForCompoundHyperlinksDialog() {
        return destinationFile.getName();
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getImageForFileWithExtension(destinationFile.getFileExtension());
    }

    @Override
    public void open() {
        final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IEditorDescriptor desc = editorRegistry.findEditor(RobotFormEditor.ID);
        try {
            final IEditorPart ed = page.openEditor(new FileEditorInput(destinationFile.getFile()), desc.getId());
            if (ed instanceof RobotFormEditor) { // it can be ErrorEditorPart if something went wrong
                final RobotFormEditor editor = (RobotFormEditor) ed;
                final SuiteSourceEditor sourcePage = editor.activateSourcePage();

                if (destination != null) {
                    sourcePage.getSelectionProvider()
                            .setSelection(new TextSelection(destination.getOffset(), destination.getLength()));
                }
            }
        } catch (final PartInitException e) {
            throw new RobotEditorOpeningException("Unable to open editor for file: " + destinationFile.getName(), e);
        }
    }
}
