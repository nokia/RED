/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl.old;

class TextPosition {

    private final String text;
    private final int startPosition;
    private final int endPosition;


    TextPosition(final String text, final int startPosition, final int endPosition) {
        this.text = text;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    String getFullText() {
        return text;
    }

    String getText() {
        String myText = null;
        if (startPosition >= 0) {
            if (endPosition >= 0) {
                myText = text.substring(startPosition, endPosition + 1);
            } else {
                myText = text.substring(startPosition);
            }
        }

        return myText;
    }

    int getStart() {
        return startPosition;
    }

    int getEnd() {
        return endPosition;
    }

    int getLength() {
        int length = 0;
        if (startPosition >= 0 && endPosition >= 0) {
            length = (endPosition - startPosition) + 1;
        }
        return length;
    }

    @Override
    public String toString() {
        return String.format("TextPosition [wholeText=%s, start=%s, end=%s, %s", text, startPosition, endPosition,
                getPartOfText() + "]");
    }

    private String getPartOfText() {
        String info = "";
        final int textLength = endPosition - startPosition;
        if (textLength < 3) {
            final int fromTheBegin = startPosition - 1;
            final int fromTheEnd = textLength - endPosition;
            if (fromTheBegin < fromTheEnd) {
                info = "beforeText=" + text.substring(0, startPosition) + ", text="
                        + text.substring(startPosition, endPosition + 1);
            } else {
                info = "text=" + text.substring(startPosition, endPosition + 1) + ", afterText="
                        + text.substring(endPosition + 1);
            }
        } else {
            info = "text=" + text.substring(startPosition, endPosition + 1);
        }

        return info;
    }
}
