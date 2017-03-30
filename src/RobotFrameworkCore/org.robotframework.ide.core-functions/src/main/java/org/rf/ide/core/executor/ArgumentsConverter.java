/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.util.ArrayList;
import java.util.List;

class ArgumentsConverter {

    static List<String> parseArguments(final String arguments) {
        final List<String> args = new ArrayList<>();
        final char chars[] = arguments.toCharArray();
        char previousToken = ' ';
        boolean wasQuotationMark = false;
        StringBuilder currentToken = new StringBuilder();
        for (final char c : chars) {
            if (c == ' ') {
                if (wasQuotationMark) {
                    currentToken.append(c);
                } else {
                    if (currentToken.length() > 0) {
                        args.add(currentToken.toString());
                        currentToken = new StringBuilder();
                    }
                }
            } else if (c == '\"') {
                if (previousToken == '\\') {
                    currentToken.append(c);
                } else {
                    if (wasQuotationMark) {
                        currentToken.append(c);
                        args.add(currentToken.substring(1, currentToken.length() - 1));
                        currentToken = new StringBuilder();

                        wasQuotationMark = false;
                    } else {
                        currentToken.append(c);
                        wasQuotationMark = true;
                    }
                }
            } else {
                currentToken.append(c);
            }
            previousToken = c;
        }

        if (currentToken.length() > 0) {
            args.add(currentToken.toString());
        }
        return args;
    }

    static boolean isSwitchArgument(final String arg) {
        return arg.startsWith("-");
    }
}
