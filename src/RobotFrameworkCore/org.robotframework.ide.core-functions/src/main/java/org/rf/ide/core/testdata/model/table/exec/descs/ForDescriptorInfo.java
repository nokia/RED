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
        ForDescriptorInfo info = new ForDescriptorInfo();

        int separatorsNumbers = 0;
        SeparatorType separatorType = SeparatorType.TABULATOR_OR_DOUBLE_SPACE;
        int numberOfElements = elements.size();
        for (int elementIndex = 0; elementIndex < numberOfElements; elementIndex++) {
            IRobotLineElement elem = elements.get(elementIndex);
            if (elem instanceof RobotToken) {
                RobotToken token = (RobotToken) elem;
                if (separatorType == SeparatorType.PIPE) {
                    if (separatorsNumbers == 2) {
                        tryToFindFor(info, elementIndex, token);
                    } else if (separatorsNumbers > 2) {
                        String text = token.getText().toString();
                        boolean shouldBreak = tryToFindPreviousLineContinoue(info, elementIndex, text);

                        if (shouldBreak) {
                            break;
                        }
                    }
                } else {
                    if (separatorsNumbers == 1) {
                        tryToFindFor(info, elementIndex, token);
                    } else if (separatorsNumbers > 1) {
                        String text = token.getText().toString();
                        boolean shouldBreak = tryToFindPreviousLineContinoue(info, elementIndex, text);

                        if (shouldBreak) {
                            break;
                        }
                    }
                }
            } else {
                Separator sep = (Separator) elem;
                if (sep.getTypes().contains(SeparatorType.PIPE) && separatorsNumbers == 0) {
                    separatorType = SeparatorType.PIPE;
                }
                separatorsNumbers++;
            }
        }

        return info;
    }

    private static void tryToFindFor(ForDescriptorInfo info, int elementIndex, RobotToken token) {
        if (isForToken(token)) {
            info.setForStartIndex(elementIndex);
        }
    }

    public static boolean isForToken(final RobotToken token) {
        String text = trimWhitespaces(token.getText().toString());
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

    private static boolean tryToFindPreviousLineContinoue(ForDescriptorInfo info, int elementIndex, String text) {
        boolean shouldBreak = false;
        if (text != null) {
            text = text.trim().toLowerCase();
            if (isInToken(text)) {
                info.setForInIndex(elementIndex);
                shouldBreak = true;
            } else if (RobotTokenType.PREVIOUS_LINE_CONTINUE.getRepresentation().get(0).equals(text)) {
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
        StringBuilder builder = new StringBuilder("");
        if (text != null) {
            char[] tChars = text.toCharArray();
            for (char c : tChars) {
                if (c != ' ' && c != '\t' && c != '\f') {
                    builder.append(c);
                }
            }
        }

        return builder.toString();
    }
}
