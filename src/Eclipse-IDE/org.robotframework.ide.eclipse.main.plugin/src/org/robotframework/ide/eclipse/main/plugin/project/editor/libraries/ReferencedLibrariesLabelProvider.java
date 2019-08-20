/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.RedCommonLabelProvider;

class ReferencedLibrariesLabelProvider extends RedCommonLabelProvider {

    private final RedProjectEditorInput editorInput;

    public ReferencedLibrariesLabelProvider(final RedProjectEditorInput editorInput) {
        this.editorInput = editorInput;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof LibraryStyledElement) {
            return ((LibraryStyledElement) element).getStyledText(editorInput);
        } else {
            return ((ElementAddingToken) element).getStyledText();
        }
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof LibraryStyledElement) {
            return ((LibraryStyledElement) element).getImage(editorInput);
        } else {
            return ((ElementAddingToken) element).getImage();
        }
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof LibraryStyledElement) {
            return ((LibraryStyledElement) element).getToolTip(editorInput);
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof LibraryStyledElement) {
            return ((LibraryStyledElement) element).getToolTipImage(editorInput);
        }
        return null;
    }

    static interface LibraryStyledElement {

        StyledString getStyledText(RedProjectEditorInput editorInput);

        Image getImage(RedProjectEditorInput editorInput);

        default String getToolTip(@SuppressWarnings("unused") final RedProjectEditorInput editorInput) {
            return null;
        }

        default Image getToolTipImage(@SuppressWarnings("unused") final RedProjectEditorInput editorInput) {
            return null;
        }
    }
}
