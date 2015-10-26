/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors;

import java.util.List;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


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
                        String text = trimWhitespaces(token.getText()
                                .toString());
                        if (":for".equalsIgnoreCase(text)) {
                            info.setForStartIndex(elementIndex);
                        }
                    } else if (separatorsNumbers > 2) {
                        String text = token.getText().toString();
                        if (text != null) {
                            text = text.trim().toLowerCase();
                            if (text.startsWith("in ")
                                    || (text.endsWith("in") && text.length() == 2)) {
                                info.setForInIndex(elementIndex);
                                break;
                            } else if (RobotTokenType.PREVIOUS_LINE_CONTINUE
                                    .getRepresentation().get(0).equals(text)) {
                                if (info.getForLineContinueInlineIndex() == -1) {
                                    info.setForLineContinueInlineIndex(elementIndex);
                                }
                            }
                        }
                    }
                } else {
                    if (separatorsNumbers == 1) {
                        String text = trimWhitespaces(token.getText()
                                .toString());
                        if (":for".equalsIgnoreCase(text)) {
                            info.setForStartIndex(elementIndex);
                        }
                    } else if (separatorsNumbers > 1) {
                        String text = token.getText().toString();
                        if (text != null) {
                            text = text.trim().toLowerCase();
                            if (text.startsWith("in ")
                                    || (text.endsWith("in") && text.length() == 2)) {
                                info.setForInIndex(elementIndex);
                                break;
                            } else if (RobotTokenType.PREVIOUS_LINE_CONTINUE
                                    .getRepresentation().get(0).equals(text)) {
                                if (info.getForLineContinueInlineIndex() == -1) {
                                    info.setForLineContinueInlineIndex(elementIndex);
                                }
                            }
                        }
                    }
                }
            } else {
                Separator sep = (Separator) elem;
                if (sep.getTypes().contains(SeparatorType.PIPE)
                        && separatorsNumbers == 0) {
                    separatorType = SeparatorType.PIPE;
                }
                separatorsNumbers++;
            }
        }

        return info;
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


    public void setForLineContinueInlineIndex(
            final int forLineContinueInlineIndex) {
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
