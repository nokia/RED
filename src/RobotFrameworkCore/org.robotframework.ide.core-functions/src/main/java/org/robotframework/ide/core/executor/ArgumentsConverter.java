package org.robotframework.ide.core.executor;

import java.util.ArrayList;
import java.util.List;

class ArgumentsConverter {

    static List<String> fromJavaArgsToPythonLike(final List<String> javaLikeArgs) {
        final List<String> args = new ArrayList<String>();

        StringBuilder current = new StringBuilder();
        for (final String arg : javaLikeArgs) {
            if (arg.startsWith("-")) {
                if (current.length() > 0) {
                    args.add(current.toString());
                    current = new StringBuilder();
                }

                args.add(arg);
            } else {
                if (current.length() > 0) {
                    current.append(' ');
                }
                current.append(arg);
            }
        }

        if (current.length() > 0) {
            args.add(current.toString());
        }

        return args;
    }

    static List<String> convertToJavaMainLikeArgs(final String arguments) {
        final List<String> args = new ArrayList<String>();
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
                        args.add(currentToken.toString());
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
}
