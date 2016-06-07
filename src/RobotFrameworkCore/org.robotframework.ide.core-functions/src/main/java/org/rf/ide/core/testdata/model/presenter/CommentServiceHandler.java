/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.base.Joiner;

/**
 * @author wypych
 */
public class CommentServiceHandler {

    public static String consolidate(final ICommentHolder comment, final ITokenSeparatorPresenter separator) {
        final StringBuilder text = new StringBuilder();

        final List<RobotToken> commentTokens = comment.getComment();
        final int nrTokens = commentTokens.size();
        if (nrTokens > 0) {
            text.append(escape(commentTokens.get(0).getText(), separator));

            for (int tokId = 1; tokId < nrTokens; tokId++) {
                text.append(separator.getSeparatorAsText());
                text.append(escape(commentTokens.get(tokId).getText(), separator));
            }
        }

        return text.toString();
    }

    private static String escape(final String text, final ITokenSeparatorPresenter separator) {
        String escaped = "";

        final List<String> textToEscape = separator.splitTextFromViewBySeparator(text);
        if (textToEscape.size() > 1) {
            escaped = Joiner.on("\\" + separator.getSeparatorAsText()).join(textToEscape);
        } else {
            escaped = text;
        }

        return escaped;
    }

    public static void update(final ICommentHolder comment, final ITokenSeparatorPresenter separator,
            final String newComment) {
        if (newComment == null || newComment.isEmpty()) {
            comment.clearComment();
        } else {
            if (!consolidate(comment, separator).equals(newComment)) {
                comment.clearComment();
                final List<String> toBeConvertedToTokens = separator.splitTextFromViewBySeparator(newComment);

                for (final String tok : toBeConvertedToTokens) {
                    RobotToken cTok = new RobotToken();
                    cTok.setText(unescape(tok, separator));
                    comment.addCommentPart(cTok);
                }
            }
        }
    }

    private static String unescape(final String text, final ITokenSeparatorPresenter separator) {
        String unescaped = "";

        final List<String> textToEscape = separator.splitTextByEscapedSeparator(text);
        if (textToEscape.size() > 1) {
            unescaped = Joiner.on(separator.getSeparatorAsText()).join(textToEscape);
        } else {
            unescaped = text;
        }

        return unescaped;
    }

    public interface ITokenSeparatorPresenter {

        String getSeparatorAsText();

        List<String> splitTextFromViewBySeparator(final String text);

        List<String> splitTextByEscapedSeparator(final String text);
    }

    public enum ETokenSeparator implements ITokenSeparatorPresenter {
        PIPE_WRAPPED_WITH_SPACE {

            @Override
            public String getSeparatorAsText() {
                return " | ";
            }

            @Override
            public List<String> splitTextFromViewBySeparator(final String text) {
                return splitByCriteria(text, false);
            }

            @Override
            public List<String> splitTextByEscapedSeparator(final String text) {
                return splitByCriteria(text, true);
            }

            private List<String> splitByCriteria(final String text, boolean shouldContainsEscape) {
                final List<String> splitted = new ArrayList<>();

                final int separatorSize = getSeparatorAsText().length();
                final int toCut = (shouldContainsEscape) ? separatorSize + 1 : separatorSize;
                final char[] chars = text.toCharArray();
                StringBuilder current = new StringBuilder("");

                for (char c : chars) {
                    current.append(c);

                    if (c == ' ') {
                        if (current.length() >= separatorSize) {
                            final String textToCheck = getTextForSeparatorCheck(current);
                            if (xnor(shouldContainsEscape, textToCheck.startsWith("\\"))
                                    && textToCheck.endsWith(getSeparatorAsText())) {
                                splitted.add(current.substring(0, current.length() - toCut));
                                current = new StringBuilder("");
                            }
                        }
                    }
                }

                splitted.add(current.toString());

                return splitted;
            }

            private boolean xnor(final boolean x1, final boolean x2) {
                return !(x1 ^ x2);
            }

            private String getTextForSeparatorCheck(final StringBuilder b) {
                String text = b.toString();
                final int length = b.length();
                if (length > 3) {
                    text = b.substring(length - 4, length);
                }

                return text;
            }
        }
    }
}
