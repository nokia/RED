/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.ToggleCommentHandler.E4ToggleCommentHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 */
public class ToggleCommentHandler extends DIParameterizedHandler<E4ToggleCommentHandler> {

    public ToggleCommentHandler() {
        super(E4ToggleCommentHandler.class);
    }

    public static class E4ToggleCommentHandler {

        @Execute
        public Object toggleComment(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor) {
            final SourceViewer viewer = editor.getSourceEditor().getViewer();
            final IDocument document = viewer.getDocument();
            final ITextSelection selection = (ITextSelection) viewer.getSelection();

            toggleComment(document, selection);

            return null;
        }

        @VisibleForTesting
        void toggleComment(final IDocument document, final ITextSelection selection) {
            final int selectionStartLine = selection.getStartLine();
            final int selectionEndLine = selection.getEndLine();

            try {
                final IRegion startLineInfo = document.getLineInformation(selectionStartLine);
                final IRegion endLineInfo = document.getLineInformation(selectionEndLine);

                final int affectedRegionOffset = startLineInfo.getOffset();
                final int affectedRegionLength = endLineInfo.getOffset() + endLineInfo.getLength()
                        - affectedRegionOffset;

                final String affectedRegionContent = document.get(affectedRegionOffset, affectedRegionLength);

                final boolean shouldAddMarks = shouldAddCommentMarks(affectedRegionContent);
                final String newContent = shouldAddMarks ? addCommentMarks(affectedRegionContent)
                        : removeCommentMarks(affectedRegionContent);

                document.replace(affectedRegionOffset, affectedRegionLength, newContent);

            } catch (final BadLocationException e) {
                // we'll do nothing then
            }
        }

        private boolean shouldAddCommentMarks(final String affectedRegionContent) {

            try (BufferedReader reader = new BufferedReader(new StringReader(affectedRegionContent))) {
                String line = reader.readLine();
                while (line != null) {
                    if (!line.trim().startsWith("#") && !line.trim().isEmpty()) {
                        return true;
                    }
                    line = reader.readLine();
                }
            } catch (final IOException e) {
                return true;
            }
            return false;
        }

        private String addCommentMarks(final String oldContent) {
            final StringBuilder newContent = new StringBuilder();
            
            boolean addedInCurrentLine = false;
            for (int i = 0; i < oldContent.length(); i++) {
                final char ch = oldContent.charAt(i);

                if (ch == '\n' || ch == '\r') {
                    addedInCurrentLine = false;
                } else if (ch != ' ' && ch != '\t' && !addedInCurrentLine) {
                    newContent.append("# ");
                    addedInCurrentLine = true;
                }
                newContent.append(ch);
            }

            return newContent.toString();
        }

        private String removeCommentMarks(final String oldContent) {
            final StringBuilder newContent = new StringBuilder();

            boolean removedInCurrentLine = false;
            for (int i = 0; i < oldContent.length(); i++) {
                final char ch = oldContent.charAt(i);

                if (ch == '\n' || ch == '\r') {
                    newContent.append(ch);
                    removedInCurrentLine = false;
                } else if (ch == '#' && !removedInCurrentLine) {
                    if (i + 1 < oldContent.length() && oldContent.charAt(i + 1) == ' ') {
                        i++;
                    }
                    removedInCurrentLine = true;
                } else {
                    newContent.append(ch);
                }
            }

            return newContent.toString();
        }
    }
}
