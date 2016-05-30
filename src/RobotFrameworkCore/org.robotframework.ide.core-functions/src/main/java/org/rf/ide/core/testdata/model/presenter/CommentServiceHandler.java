/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter;

import java.util.List;

import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.base.Joiner;
import com.google.common.collect.Range;

public class CommentServiceHandler {

    public static String consolidate(final ICommentHolder commentElement, final String tokenSeparator) {
        final StringBuilder builder = new StringBuilder("");

        final List<RobotToken> comments = commentElement.getComment();
        int size = comments.size();
        if (size > 0) {
            final Range<Integer> tokenSeparatorRange = Range.open(0, size);
            for (int index = 0; index < size; index++) {
                final RobotToken rToken = comments.get(index);

                if (tokenSeparatorRange.contains(index)) {
                    builder.append(tokenSeparator);
                }

                builder.append(escape(rToken.getText(), tokenSeparator));
            }
        }

        return builder.toString();
    }

    public static void update(final ICommentHolder commentElement, final String newComment,
            final String tokenSeparator) {
        String currentText = consolidate(commentElement, tokenSeparator);
        if (!currentText.equals(newComment)) {

        }
    }

    public static String escape(final String commentText, final String tokenSeparator) {
        final String commentPartSep = tokenSeparator.trim();
        String escaped = commentText;
        final String[] toEscape = commentText.split("(?!\\\\[" + commentPartSep + "])[" + commentPartSep + "]");
        final int escapedSize = toEscape.length;
        if (escapedSize > 1) {
            escaped = Joiner.on("\\" + commentPartSep).join(toEscape);
        }

        return escaped;
    }

    public static String unescape(final String commentText, final String tokenSeparator) {
        final String commentPartSep = tokenSeparator.trim();
        String unescaped = commentText;
        final String[] toUnEscape = commentText.split("\\\\[" + commentPartSep + "]");
        final int escapedSize = toUnEscape.length;
        if (escapedSize > 1) {
            unescaped = Joiner.on(commentPartSep).join(toUnEscape);
        }

        return unescaped;
    }
}
