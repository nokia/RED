/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;


public class TextEditorHoverManager {

    public IRegion findHoveredText(final IDocument document, final int offset) {

        int length = 0;
        int beginOffset = offset;
        final String firstCharacter = getTextRange(document, offset, 1);
        if (!Character.isWhitespace(firstCharacter.charAt(0))) {
            length = 1;
            while (!isSeparator(getTextRange(document, offset + length, 2))) {
                length++;
            }
            int j = offset - 1;
            while (!isSeparatorInReverseSearching(getTextRangeInReverseSearching(document, j - 1, 2))) {
                beginOffset--;
                j--;
                length++;
            }
        }

        return new Region(beginOffset, length);
    }
    
    public String extractDebugVariableHoverInfo(final Map<String, Object> debugVariables, String hoveredText) {
        if (hoveredText.indexOf("}=") >= 0 || hoveredText.indexOf("} =") >= 0) {
            hoveredText = hoveredText.substring(0, hoveredText.lastIndexOf("}") + 1);
        }
        final Object value = debugVariables.get(hoveredText);
        return value != null ? value.toString() : null;
    }

    
    private boolean isSeparator(final String s) {
        if (s.substring(0, 1).equals(" ") && !Character.isWhitespace(s.charAt(1))) {
            return false;
        } else if (Character.isWhitespace(s.charAt(0))) {
            return true;
        }
        return false;
    }

    private boolean isSeparatorInReverseSearching(final String s) {
        if (s.substring(1, 2).equals(" ") && !Character.isWhitespace(s.charAt(0))) {
            return false;
        } else if (Character.isWhitespace(s.charAt(1))) {
            return true;
        }
        return false;
    }

    private String getTextRange(final IDocument document, final int offset, final int length) {
        String result = "  ";
        try {
            return document.get(offset, length);
        } catch (final BadLocationException e) {
            result = getTextRange(document, offset, length - 1) + " ";
        }
        return result;
    }

    private String getTextRangeInReverseSearching(final IDocument document, final int offset, final int length) {
        String result = "  ";
        try {
            return document.get(offset, length);
        } catch (final BadLocationException e) {
            result = " " + getTextRangeInReverseSearching(document, offset + 1, length - 1);
        }
        return result;
    }
}
