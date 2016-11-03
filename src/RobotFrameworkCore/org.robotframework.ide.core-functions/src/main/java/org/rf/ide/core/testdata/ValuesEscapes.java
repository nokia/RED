/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata;

public class ValuesEscapes {

    public static String unescapeSpaces(final String str) {
        int backslashes = 0;
        final StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            escaped.append(c);
            if (c == '\\') {
                backslashes++;
            } else if (c == ' ' && backslashes % 2 == 1) {
                backslashes = 0;
                escaped.deleteCharAt(escaped.length() - 2);
            } else {
                backslashes = 0;
            }
        }
        return escaped.toString();
    }
}

