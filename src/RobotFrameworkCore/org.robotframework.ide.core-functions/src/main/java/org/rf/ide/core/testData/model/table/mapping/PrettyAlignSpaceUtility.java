/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testData.model.table.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testData.model.FilePosition;
import org.rf.ide.core.testData.model.RobotFileOutput;
import org.rf.ide.core.testData.text.read.IRobotLineElement;
import org.rf.ide.core.testData.text.read.IRobotTokenType;
import org.rf.ide.core.testData.text.read.ParsingState;
import org.rf.ide.core.testData.text.read.RobotLine;
import org.rf.ide.core.testData.text.read.recognizer.RobotToken;
import org.rf.ide.core.testData.text.read.recognizer.RobotTokenType;


public class PrettyAlignSpaceUtility {

    private final ParsingStateHelper stateHelper;


    public PrettyAlignSpaceUtility() {
        this.stateHelper = new ParsingStateHelper();
    }


    public void fixOnlyPrettyAlignLinesInSettings(final RobotLine line,
            final Stack<ParsingState> processingState) {
        final ParsingState state = stateHelper
                .getCurrentStatus(processingState);
        if (state == ParsingState.SETTING_TABLE_INSIDE) {
            removeTokenWithoutTextFromSimpleTableLine(line);
        }
    }


    public void fixOnlyPrettyAlignLinesInVariables(final RobotLine line,
            final Stack<ParsingState> processingState) {
        final ParsingState state = stateHelper
                .getCurrentStatus(processingState);
        if (state == ParsingState.VARIABLE_TABLE_INSIDE) {
            removeTokenWithoutTextFromSimpleTableLine(line);
        }
    }


    private void removeTokenWithoutTextFromSimpleTableLine(final RobotLine line) {
        boolean containsAnyValuableToken = false;
        final List<Integer> emptyStrings = new ArrayList<>();
        final List<IRobotLineElement> lineElements = line.getLineElements();
        final int length = lineElements.size();
        for (int lineElementIndex = 0; lineElementIndex < length; lineElementIndex++) {
            final IRobotLineElement elem = lineElements.get(lineElementIndex);
            if (elem instanceof RobotToken) {
                final RobotToken rt = (RobotToken) elem;
                final List<IRobotTokenType> types = rt.getTypes();
                for (final IRobotTokenType type : types) {
                    if (type != RobotTokenType.UNKNOWN
                            && type != RobotTokenType.PRETTY_ALIGN_SPACE) {
                        containsAnyValuableToken = true;
                    }
                }

                final String text = rt.getRaw().toString();
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
            final int emptiesSize = emptyStrings.size();
            for (int index = emptiesSize - 1; index >= 0; index--) {
                lineElements.remove((int) emptyStrings.get(index));
            }
        }
    }


    public void extractPrettyAlignWhitespaces(final RobotLine line,
            final RobotToken rt, final String rawText) {
        final boolean isNotPrettyAlign = !rt.getTypes().contains(
                RobotTokenType.PRETTY_ALIGN_SPACE);
        String correctedString = rawText;
        if (rawText.startsWith(" ") && isNotPrettyAlign) {
            final RobotToken prettyLeftAlign = new RobotToken();
            prettyLeftAlign.setStartOffset(rt.getStartOffset());
            prettyLeftAlign.setLineNumber(rt.getLineNumber());
            prettyLeftAlign.setStartColumn(rt.getStartColumn());
            prettyLeftAlign.setRaw(" ");
            prettyLeftAlign.setText(" ");
            prettyLeftAlign.setType(RobotTokenType.PRETTY_ALIGN_SPACE);
            line.addLineElementAt(line.getLineElements().size() - 1,
                    prettyLeftAlign);

            rt.setStartColumn(rt.getStartColumn() + 1);
            rt.setStartOffset(rt.getStartOffset() + 1);
            correctedString = rawText.substring(1);
            rt.setText(correctedString);
            rt.setRaw(correctedString);
        }

        if (correctedString.endsWith(" ") && isNotPrettyAlign) {
            final int theLongestTextLength = Math.max(rt.getRaw().length(),
                    rawText.length());
            final RobotToken prettyRightAlign = new RobotToken();
            prettyRightAlign.setStartOffset(rt.getStartOffset()
                    + theLongestTextLength - 1);
            prettyRightAlign.setLineNumber(rt.getLineNumber());
            prettyRightAlign.setStartColumn(theLongestTextLength - 1);
            prettyRightAlign.setRaw(" ");
            prettyRightAlign.setText(" ");
            prettyRightAlign.setType(RobotTokenType.PRETTY_ALIGN_SPACE);
            line.addLineElement(prettyRightAlign);

            correctedString = correctedString.substring(0,
                    correctedString.length() - 1);
            rt.setText(correctedString);
            rt.setRaw(correctedString);
        }
    }


    public RobotToken applyPrettyAlignTokenIfIsValid(
            final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final FilePosition fp,
            final String text, final String fileName,
            final RobotToken robotToken) {
        if (" ".equals(text)) {
            boolean isPrettyAlign = false;

            final ParsingState currentStatus = stateHelper
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
