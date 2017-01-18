/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import org.eclipse.jface.text.rules.ICharacterScanner;

import com.google.common.base.Joiner;

class CharacterScanner implements ICharacterScanner {

    private final char[] content;

    private int offset = 0;

    CharacterScanner(final String... lines) {
        this.content = Joiner.on('\n').join(lines).toCharArray();
    }

    @Override
    public char[][] getLegalLineDelimiters() {
        return new char[][] { new char[] { '\n' } };
    }

    @Override
    public int getColumn() {
        int column = 0;
        for (int i = offset - 1; i >= 0; i--) {
            if (i < content.length && content[i] == '\n') {
                return column;
            }
            column++;
        }
        return column;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public int read() {
        if (offset >= content.length) {
            offset++;
            return ICharacterScanner.EOF;
        }
        return content[offset++];
    }

    @Override
    public void unread() {
        offset = Math.max(offset - 1, 0);
    }
}
