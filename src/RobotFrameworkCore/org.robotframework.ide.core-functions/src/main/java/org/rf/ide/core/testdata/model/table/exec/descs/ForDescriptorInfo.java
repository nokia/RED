/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs;

import java.util.List;

import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;

public class ForDescriptorInfo {

    private int forStartIndex = -1;

    private int forLineContinueInlineIndex = -1;

    private int forInIndex = -1;

    public static ForDescriptorInfo build(final List<IRobotLineElement> elements) {
        final ForDescriptorInfo info = new ForDescriptorInfo();

        int separatorsNumbers = 0;
        SeparatorType separatorType = SeparatorType.TABULATOR_OR_DOUBLE_SPACE;
        final int numberOfElements = elements.size();
        for (int elementIndex = 0; elementIndex < numberOfElements; elementIndex++) {
            final IRobotLineElement elem = elements.get(elementIndex);
            if (elem instanceof RobotToken) {
                final RobotToken token = (RobotToken) elem;
                if (separatorType == SeparatorType.PIPE) {
                    if (separatorsNumbers == 2) {
                        tryToFindFor(info, elementIndex, token);
                    } else if (separatorsNumbers > 2) {
                        final String text = token.getText().toString();
                        final boolean shouldBreak = tryToFindPreviousLineContinoue(info, elementIndex, text);

                        if (shouldBreak) {
                            break;
                        }
                    }
                } else {
                    if (separatorsNumbers == 1) {
                        tryToFindFor(info, elementIndex, token);
                    } else if (separatorsNumbers > 1) {
                        final String text = token.getText().toString();
                        final boolean shouldBreak = tryToFindPreviousLineContinoue(info, elementIndex, text);

                        if (shouldBreak) {
                            break;
                        }
                    }
                }
            } else {
                final Separator sep = (Separator) elem;
                if (sep.getTypes().contains(SeparatorType.PIPE) && separatorsNumbers == 0) {
                    separatorType = SeparatorType.PIPE;
                }
                separatorsNumbers++;
            }
        }

        return info;
    }

    private static void tryToFindFor(final ForDescriptorInfo info, final int elementIndex, final RobotToken token) {
        if (isForToken(token)) {
            info.setForStartIndex(elementIndex);
        }
    }

    public static boolean isForToken(final RobotToken token) {
        final String text = trimWhitespaces(token.getText().toString());
        return ":for".equalsIgnoreCase(text);
    }

    public static boolean isInToken(final RobotToken token) {
        String text = token.getText().toString();
        text = text.trim().toLowerCase();
        return isInToken(text);
    }

    private static boolean isInToken(final String text) {
        boolean isInToken = false;
        final List<String> inRepresentations = RobotTokenType.IN_TOKEN.getRepresentation();
        for (final String r : inRepresentations) {
            if (r.equalsIgnoreCase(text)) {
                isInToken = true;
                break;
            }
        }

        return isInToken;
    }

    private static boolean tryToFindPreviousLineContinoue(final ForDescriptorInfo info, final int elementIndex,
            final String text) {
        boolean shouldBreak = false;
        if (text != null) {
            final String normalizedText = text.trim().toLowerCase();
            if (isInToken(normalizedText)) {
                info.setForInIndex(elementIndex);
                shouldBreak = true;
            } else if (RobotTokenType.PREVIOUS_LINE_CONTINUE.getRepresentation().get(0).equals(normalizedText)) {
                if (info.getForLineContinueInlineIndex() == -1) {
                    info.setForLineContinueInlineIndex(elementIndex);
                }
            }
        }
        return shouldBreak;
    }

    private void setForStartIndex(final int forStartIndex) {
        this.forStartIndex = forStartIndex;
    }

    public int getForStartIndex() {
        return forStartIndex;
    }

    public int getForLineContinueInlineIndex() {
        return forLineContinueInlineIndex;
    }

    public void setForLineContinueInlineIndex(final int forLineContinueInlineIndex) {
        this.forLineContinueInlineIndex = forLineContinueInlineIndex;
    }

    private void setForInIndex(final int forInIndex) {
        this.forInIndex = forInIndex;
    }

    public int getForInIndex() {
        return forInIndex;
    }

    private static String trimWhitespaces(final String text) {
        final StringBuilder builder = new StringBuilder("");
        if (text != null) {
            final char[] tChars = text.toCharArray();
            for (final char c : tChars) {
                if (c != ' ' && c != '\t' && c != '\f') {
                    builder.append(c);
                }
            }
        }

        return builder.toString();
    }
}
