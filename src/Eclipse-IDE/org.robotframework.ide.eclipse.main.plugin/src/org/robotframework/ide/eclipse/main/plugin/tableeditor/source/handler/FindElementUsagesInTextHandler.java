package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */

import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISources;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.FindElementUsagesInTextHandler.E4FindUsagesInTextHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class FindElementUsagesInTextHandler extends DIParameterizedHandler<E4FindUsagesInTextHandler> {

    public FindElementUsagesInTextHandler() {
        super(E4FindUsagesInTextHandler.class);
    }

    public static class E4FindUsagesInTextHandler {

        @Execute
        public void findUsages(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named("org.robotframework.ide.eclipse.findElementUsages.place") final String place) {

            final SourceViewer viewer = editor.getSourceEditor().getViewer();

            final IEditorInput input = editor.getEditorInput();
            final FileEditorInput editorfile = (FileEditorInput) input;
            final IPath path = editorfile.getPath();
            final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
            boolean isTSV = file.getFileExtension().equals("tsv");

            final ITextSelection selection = (ITextSelection) viewer.getSelection();

            String selectedText = selection.getText();

            if (selectedText.isEmpty()) {
                try {
                    IDocument doc = viewer.getDocument();
                    int offset = selection.getOffset();
                    IRegion token;
                    token = doc.getLineInformation(doc.getLineOfOffset(offset));
                    selectedText = DocumentUtilities.getFirstTokenInLine(doc, isTSV, token.getOffset());
                } catch (BadLocationException e1) {
                    return;
                }
            }
            if (!selectedText.isEmpty()) {
                FindUsagesHandler.findElements(place, file, selectedText);
            }

        }

    }

}
