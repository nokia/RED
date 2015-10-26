/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.mapping.ElementPositionResolver;
import org.robotframework.ide.core.testData.model.table.mapping.ElementPositionResolver.PositionExpected;
import org.robotframework.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class PreviousLineHandler {

    private final ElementPositionResolver posResolver;
    private final ParsingStateHelper stateHelper;


    public PreviousLineHandler() {
        this.posResolver = new ElementPositionResolver();
        this.stateHelper = new ParsingStateHelper();
    }

    private final Stack<ParsingState> storedStack = new Stack<>();


    public LineContinueType computeLineContinue(
            final Stack<ParsingState> parsingStates, boolean isNewLine,
            final RobotFile model, final RobotLine currentLine,
            final RobotToken currentToken) {
        LineContinueType continueType = LineContinueType.NONE;

        if ((currentToken.getTypes().size() == 1 && currentToken.getTypes()
                .contains(RobotTokenType.PREVIOUS_LINE_CONTINUE))
                || isCommentContinue(currentToken, storedStack)) {
            ParsingState currentState = stateHelper
                    .getCurrentStatus(parsingStates);

            if (currentState == ParsingState.SETTING_TABLE_INSIDE) {
                if (isNewLine && containsAnySetting(model)
                        && isSomethingToContinue(model)) {
                    if (posResolver
                            .isCorrectPosition(
                                    PositionExpected.LINE_CONTINUE_NEWLINE_FOR_SETTING_TABLE,
                                    model, currentLine, currentToken)) {
                        continueType = LineContinueType.SETTING_TABLE_ELEMENT;
                    }
                }
            } else if (currentState == ParsingState.VARIABLE_TABLE_INSIDE) {
                if (isNewLine && containsAnyVariable(model)
                        && isSomethingToContinue(model)) {
                    if (posResolver
                            .isCorrectPosition(
                                    PositionExpected.LINE_CONTINUE_NEWLINE_FOR_VARIABLE_TABLE,
                                    model, currentLine, currentToken)) {
                        continueType = LineContinueType.VARIABLE_TABLE_ELEMENT;
                    }
                }
            } else if (currentState == ParsingState.TEST_CASE_TABLE_INSIDE
                    || currentState == ParsingState.TEST_CASE_DECLARATION) {
                if (isNewLine) {
                    if (posResolver
                            .isCorrectPosition(
                                    PositionExpected.LINE_CONTINUE_NEWLINE_FOR_TESTCASE_TABLE,
                                    model, currentLine, currentToken)) {
                        ParsingState state = stateHelper
                                .getCurrentStatus(storedStack);
                        if (state == ParsingState.TEST_CASE_TABLE_HEADER) {
                            storedStack
                                    .remove(ParsingState.TEST_CASE_TABLE_HEADER);
                            storedStack
                                    .push(ParsingState.TEST_CASE_TABLE_INSIDE);
                        }
                        continueType = LineContinueType.TEST_CASE_TABLE_ELEMENT;
                    }
                } else {
                    if (posResolver
                            .isCorrectPosition(
                                    PositionExpected.LINE_CONTINUE_INLINED_FOR_TESTCASE_TABLE,
                                    model, currentLine, currentToken)) {
                        continueType = LineContinueType.LINE_CONTINUE_INLINED;
                    }
                }
            } else if (currentState == ParsingState.KEYWORD_TABLE_INSIDE
                    || currentState == ParsingState.KEYWORD_DECLARATION) {
                if (isNewLine) {
                    if (posResolver
                            .isCorrectPosition(
                                    PositionExpected.LINE_CONTINUE_NEWLINE_FOR_KEYWORD_TABLE,
                                    model, currentLine, currentToken)) {
                        ParsingState state = stateHelper
                                .getCurrentStatus(storedStack);
                        if (state == ParsingState.KEYWORD_TABLE_HEADER) {
                            storedStack
                                    .remove(ParsingState.KEYWORD_TABLE_HEADER);
                            storedStack.push(ParsingState.KEYWORD_TABLE_INSIDE);
                        }
                        continueType = LineContinueType.KEYWORD_TABLE_ELEMENT;
                    }
                } else {
                    if (posResolver
                            .isCorrectPosition(
                                    PositionExpected.LINE_CONTINUE_INLINED_FOR_KEYWORD_TABLE,
                                    model, currentLine, currentToken)) {
                        continueType = LineContinueType.LINE_CONTINUE_INLINED;
                    }
                }
            }
        }

        return continueType;
    }


    @VisibleForTesting
    protected boolean isCommentContinue(RobotToken currentToken,
            Stack<ParsingState> storedStack) {
        boolean result = false;

        if (currentToken.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
            if (!storedStack.isEmpty()) {
                result = storedStack.get(storedStack.size() - 1) == ParsingState.COMMENT;
            }
        }

        return result;
    }


    public boolean isSomethingToContinue(final RobotFile model) {
        boolean result = false;
        List<RobotLine> fileContent = model.getFileContent();
        boolean notFoundYet = true;
        for (int i = fileContent.size() - 1; i > 0 && notFoundYet; i--) {
            RobotLine robotLine = fileContent.get(i);
            List<IRobotLineElement> lineElements = robotLine.getLineElements();
            if (!lineElements.isEmpty()) {
                for (int k = 0; k < lineElements.size() && k < 2; k++) {
                    IRobotLineElement elem = lineElements.get(k);
                    List<IRobotTokenType> types = elem.getTypes();
                    if (types.contains(RobotTokenType.KEYWORDS_TABLE_HEADER)
                            || types.contains(RobotTokenType.SETTINGS_TABLE_HEADER)
                            || types.contains(RobotTokenType.VARIABLES_TABLE_HEADER)
                            || types.contains(RobotTokenType.TEST_CASES_TABLE_HEADER)) {
                        result = false;
                        notFoundYet = false;
                        break;
                    } else if (types.contains(SeparatorType.PIPE)
                            || types.contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE)
                            || types.contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)
                            || types.contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                        continue;
                    } else {
                        result = true;
                        notFoundYet = false;
                        break;
                    }
                }
            }
        }

        return result;
    }


    @VisibleForTesting
    protected boolean containsAnySetting(final RobotFile file) {
        return !file.getSettingTable().isEmpty();
    }


    @VisibleForTesting
    protected boolean containsAnyVariable(final RobotFile file) {
        return !file.getVariableTable().getVariables().isEmpty();
    }


    @VisibleForTesting
    protected boolean containsAnyTestCases(final RobotFile file) {
        return !file.getTestCaseTable().getTestCases().isEmpty();
    }


    @VisibleForTesting
    protected boolean containsAnyKeywords(final RobotFile file) {
        return !file.getKeywordTable().getKeywords().isEmpty();
    }


    public boolean isSomethingToDo(final LineContinueType type) {
        return (type != LineContinueType.NONE);
    }


    public void restorePreviousStack(final LineContinueType continueType,
            final Stack<ParsingState> parsingStates,
            final RobotLine currentLine, final RobotToken currentToken) {
        if (isSomethingToDo(continueType)) {
            parsingStates.clear();
            removeLastNotWantedStates(storedStack);
            parsingStates.addAll(storedStack);
        }
    }


    public void flushNew(final Stack<ParsingState> parsingStates) {
        clear();
        storedStack.addAll(parsingStates);
    }


    public void clear() {
        storedStack.clear();
    }

    public static enum LineContinueType {
        NONE, SETTING_TABLE_ELEMENT, VARIABLE_TABLE_ELEMENT, TEST_CASE_TABLE_ELEMENT, KEYWORD_TABLE_ELEMENT, LINE_CONTINUE_INLINED;
    }


    @VisibleForTesting
    protected void removeLastNotWantedStates(
            final Stack<ParsingState> parsingStates) {
        for (int i = parsingStates.size() - 1; i >= 0; i--) {
            ParsingState state = parsingStates.get(i);
            if (state == ParsingState.COMMENT) {
                parsingStates.remove(i);
            } else {
                break;
            }
        }
    }
}
