/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;

/**
 * @author wypych
 */
public class StyledTextCaretPositionProvider {

    public static int getOffset(final StyledText text, final Point point) {
        final int charCount = text.getCharCount();
        if (charCount > 0 && text.getTextBounds(0, charCount - 1).contains(point)) {
            try {
                return text.getOffsetAtLocation(point);
            } catch (final IllegalArgumentException e) {
                final int beginOfLine = text.getOffsetAtLocation(new Point(0, point.y));
                final int lineNumber = text.getLineAtOffset(beginOfLine);
                final int lineTextLength = text.getLine(lineNumber).length();
                return (beginOfLine + lineTextLength);
            }
        } else {
            return charCount;
        }
    }
}
