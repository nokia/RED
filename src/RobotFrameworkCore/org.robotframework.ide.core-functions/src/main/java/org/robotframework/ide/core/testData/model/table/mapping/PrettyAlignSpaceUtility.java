/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.mapping;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class PrettyAlignSpaceUtility {

    private final ParsingStateHelper stateHelper;


    public PrettyAlignSpaceUtility() {
        this.stateHelper = new ParsingStateHelper();
    }


    public void fixOnlyPrettyAlignLinesInSettings(final RobotLine line,
            final Stack<ParsingState> processingState) {
        ParsingState state = stateHelper.getCurrentStatus(processingState);
        if (state == ParsingState.SETTING_TABLE_INSIDE) {
            removeTokenWithoutTextFromSimpleTableLine(line);
        }
    }


    public void fixOnlyPrettyAlignLinesInVariables(final RobotLine line,
            final Stack<ParsingState> processingState) {
        ParsingState state = stateHelper.getCurrentStatus(processingState);
        if (state == ParsingState.VARIABLE_TABLE_INSIDE) {
            removeTokenWithoutTextFromSimpleTableLine(line);
        }
    }


    private void removeTokenWithoutTextFromSimpleTableLine(final RobotLine line) {
        boolean containsAnyValuableToken = false;
        List<Integer> emptyStrings = new LinkedList<>();
        List<IRobotLineElement> lineElements = line.getLineElements();
        int length = lineElements.size();
        for (int lineElementIndex = 0; lineElementIndex < length; lineElementIndex++) {
            IRobotLineElement elem = lineElements.get(lineElementIndex);
            if (elem instanceof RobotToken) {
                RobotToken rt = (RobotToken) elem;
                List<IRobotTokenType> types = rt.getTypes();
                for (IRobotTokenType type : types) {
                    if (type != RobotTokenType.UNKNOWN
                            && type != RobotTokenType.PRETTY_ALIGN_SPACE) {
                        containsAnyValuableToken = true;
                    }
                }

                String text = rt.getRaw().toString();
                if (text != null && text.trim().length() > 0) {
                    containsAnyValuableToken = true;
                } else if (!containsAnyValuableToken
                        && types.contains(RobotTokenType.UNKNOWN)) {
                    emptyStrings.add(lineElementIndex);
                }
            }
        }

        if (!containsAnyValuableToken) {
            Collections.sort(emptyStrings);
            int emptiesSize = emptyStrings.size();
            for (int index = emptiesSize - 1; index >= 0; index--) {
                lineElements.remove((int) emptyStrings.get(index));
            }
        }
    }


    public void extractPrettyAlignWhitespaces(RobotLine line, RobotToken rt,
            String rawText) {
        boolean isNotPrettyAlign = !rt.getTypes().contains(
                RobotTokenType.PRETTY_ALIGN_SPACE);
        String correctedString = rawText;
        if (rawText.startsWith(" ") && isNotPrettyAlign) {
            RobotToken prettyLeftAlign = new RobotToken();
            prettyLeftAlign.setStartOffset(rt.getStartOffset());
            prettyLeftAlign.setLineNumber(rt.getLineNumber());
            prettyLeftAlign.setStartColumn(rt.getStartColumn());
            prettyLeftAlign.setRaw(new StringBuilder(" "));
            prettyLeftAlign.setText(new StringBuilder(" "));
            prettyLeftAlign.setType(RobotTokenType.PRETTY_ALIGN_SPACE);
            line.addLineElementAt(line.getLineElements().size() - 1,
                    prettyLeftAlign);

            rt.setStartColumn(rt.getStartColumn() + 1);
            rt.setStartOffset(rt.getStartOffset() + 1);
            correctedString = rawText.substring(1);
            rt.setText(new StringBuilder(correctedString));
            rt.setRaw(new StringBuilder(correctedString));
        }

        if (correctedString.endsWith(" ") && isNotPrettyAlign) {
            int theLongestTextLength = Math.max(rt.getRaw().length(),
                    rawText.length());
            RobotToken prettyRightAlign = new RobotToken();
            prettyRightAlign.setStartOffset(rt.getStartOffset()
                    + theLongestTextLength - 1);
            prettyRightAlign.setLineNumber(rt.getLineNumber());
            prettyRightAlign.setStartColumn(theLongestTextLength - 1);
            prettyRightAlign.setRaw(new StringBuilder(" "));
            prettyRightAlign.setText(new StringBuilder(" "));
            prettyRightAlign.setType(RobotTokenType.PRETTY_ALIGN_SPACE);
            line.addLineElement(prettyRightAlign);

            correctedString = correctedString.substring(0,
                    correctedString.length() - 1);
            rt.setText(new StringBuilder(correctedString));
            rt.setRaw(new StringBuilder(correctedString));
        }
    }


    public RobotToken applyPrettyAlignTokenIfIsValid(RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final FilePosition fp,
            String text, String fileName, RobotToken robotToken) {
        if (" ".equals(text)) {
            boolean isPrettyAlign = false;

            ParsingState currentStatus = stateHelper
                    .getCurrentStatus(processingState);
            if (currentStatus == ParsingState.KEYWORD_TABLE_INSIDE
                    || currentStatus == ParsingState.TEST_CASE_TABLE_INSIDE
                    || currentStatus == ParsingState.TEST_CASE_DECLARATION
                    || currentStatus == ParsingState.KEYWORD_DECLARATION) {
                isPrettyAlign = true;
            }

            if (isPrettyAlign) {
                robotToken.setType(RobotTokenType.PRETTY_ALIGN_SPACE);
            }
        }

        return robotToken;
    }
}
