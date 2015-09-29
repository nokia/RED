/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import org.eclipse.jface.text.rules.ICharacterScanner;

/**
 * @author Michal Anglart
 *
 */
class CharacterScannerUtilities {

    static boolean isCellSeparator(final String string) {
        return string.startsWith("\t") || string.startsWith(" \t") || string.startsWith("  ")
                || string.startsWith(" | ") || string.startsWith(" |\t");
    }

    static String lineContentBeforeCurrentPosition(final ICharacterScanner scanner) {
        final StringBuilder builder = new StringBuilder();

        int additional = 0;
        while (true) {
            scanner.unread();
            if (scanner.getColumn() == -1) {
                additional = 1;
                break;
            }
            final int ch = scanner.read();
            if (ch == '\n') {
                break;
            }
            builder.append((char) ch);
            scanner.unread();
        }
        // maybe we've read less due to EOF
        for (int i = 0; i < builder.length() + additional; i++) {
            scanner.read();
        }
        builder.reverse();
        return builder.toString();
    }

    static String lookAhead(final ICharacterScanner scanner, final int n) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            final int ch = scanner.read();
            if (ch == -1) {
                break;
            } else {
                builder.append((char) ch);
            }
        }
        // maybe we've read less due to EOF
        for (int i = 0; i < builder.length(); i++) {
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
}
