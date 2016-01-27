/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import org.eclipse.jface.text.rules.ICharacterScanner;

/**
 * @author Michal Anglart
 */
class CharacterScannerUtilities {

    static boolean isCellSeparator(final String string) {
        return string.startsWith("\t") || string.startsWith(" \t") || string.startsWith("  ")
                || string.startsWith(" | ") || string.startsWith(" |\t");
    }

    static String lookAhead(final ICharacterScanner scanner, final int n) {
        boolean eofOccured = false;
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            final int ch = scanner.read();
            if (ch == -1) {
                eofOccured = true;
                break;
            } else {
                builder.append((char) ch);
            }
        }
        final int additional = eofOccured ? 1 : 0;
        // maybe we've read less due to EOF
        for (int i = 0; i < builder.length() + additional; i++) {
            scanner.unread();
        }
        return builder.toString();
    }

    static String lookBack(final ICharacterScanner scanner, final int n) {
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < n; i++) {
            scanner.unread();
            if (scanner.getColumn() == -1) {
                break;
            }
            final int ch = scanner.read();
            builder.append((char) ch);
            scanner.unread();
        }
        // maybe we've read less due to EOF
        for (int i = 0; i < builder.length(); i++) {
            scanner.read();
        }
        builder.reverse();
        return builder.toString();
    }

    static int eat(final ICharacterScanner scanner, final int n) {
        int ch = -1;
        for (int i = 0; i < n; i++) {
            ch = scanner.read();
        }
        return ch;
    }
}
