/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;

/**
 * @author Michal Anglart
 */
class MatchLabelCreator {

    StyledString create(final String content, final Position matchPosition) {
        final StyledString label = new StyledString();
        label.append(getLineBefore(content, matchPosition));
        label.append(getContent(content, matchPosition), Stylers.Common.ECLIPSE_SEARCH_MATCH_STYLER);
        label.append(getLineContinuation(content, matchPosition));
        return label;
    }

    private String getLineBefore(final String content, final Position matchPosition) {
        final StringBuilder builder = new StringBuilder();
        for (int i = matchPosition.getOffset() - 1; i >= 0; i--) {
            final char ch = content.charAt(i);
            if (ch == '\n' || ch == '\r') {
                break;
            }
            builder.append(ch);
        }
        return builder.reverse().toString();
    }

    private String getContent(final String content, final Position matchPosition) {
        return content.substring(matchPosition.getOffset(), matchPosition.getOffset() + matchPosition.getLength())
                .replaceAll("\n", "\\n");
    }

    private String getLineContinuation(final String content, final Position matchPosition) {
        final StringBuilder builder = new StringBuilder();
        for (int i = matchPosition.getOffset() + matchPosition.getLength(); i < content.length(); i++) {
            final char ch = content.charAt(i);
            if (ch == '\n' || ch == '\r') {
                break;
            }
            builder.append(ch);
        }
        return builder.toString();
    }
}
