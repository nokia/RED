/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver;
import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver.PositionExpected;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;

public class PreviousLineHandler {

    private final ElementPositionResolver posResolver = new ElementPositionResolver();

    private final ParsingStateHelper stateHelper = new ParsingStateHelper();

    private final Stack<ParsingState> storedStack = new Stack<>();


    public LineContinueType computeLineContinue(final Stack<ParsingState> parsingStates, final boolean isNewLine,
            final RobotFile model, final RobotLine currentLine, final RobotToken currentToken) {
        if (isPreviousLineContinueToken(currentToken) || isCommentContinue(currentToken, storedStack)) {
            final ParsingState currentState = stateHelper.getCurrentState(parsingStates);

            if (currentState == ParsingState.SETTING_TABLE_INSIDE) {
                if (isNewLine && containsAnySetting(model) && isSomethingToContinue(model)
                        && posResolver.isCorrectPosition(PositionExpected.LINE_CONTINUE_NEWLINE_FOR_SETTING_TABLE,
                                currentLine, currentToken)) {
                    return LineContinueType.SETTING_TABLE_ELEMENT;
                }
            } else if (currentState == ParsingState.VARIABLE_TABLE_INSIDE) {
                if (isNewLine && containsAnyVariable(model) && isSomethingToContinue(model)
                        && posResolver.isCorrectPosition(PositionExpected.LINE_CONTINUE_NEWLINE_FOR_VARIABLE_TABLE,
                                currentLine, currentToken)) {
                    return LineContinueType.VARIABLE_TABLE_ELEMENT;
                }
            } else if (currentState == ParsingState.TEST_CASE_TABLE_INSIDE
                    || currentState == ParsingState.TEST_CASE_DECLARATION) {

                if (isNewLine && posResolver.isCorrectPosition(
                        PositionExpected.LINE_CONTINUE_NEWLINE_FOR_TESTCASE_TABLE, currentLine, currentToken)) {
                    final ParsingState state = stateHelper.getCurrentState(storedStack);
                    if (state == ParsingState.TEST_CASE_TABLE_HEADER) {
                        storedStack.remove(ParsingState.TEST_CASE_TABLE_HEADER);
                        storedStack.push(ParsingState.TEST_CASE_TABLE_INSIDE);
                    }
                    return LineContinueType.TEST_CASE_TABLE_ELEMENT;

                } else if (posResolver.isCorrectPosition(PositionExpected.LINE_CONTINUE_INLINED_FOR_TESTCASE_TABLE,
                        currentLine, currentToken)) {
                    return LineContinueType.LINE_CONTINUE_INLINED;
                }
            } else if (currentState == ParsingState.TASKS_TABLE_INSIDE
                    || currentState == ParsingState.TASK_DECLARATION) {

                if (isNewLine && posResolver.isCorrectPosition(
                        PositionExpected.LINE_CONTINUE_NEWLINE_FOR_TASK_TABLE, currentLine, currentToken)) {
                    final ParsingState state = stateHelper.getCurrentState(storedStack);
                    if (state == ParsingState.TASKS_TABLE_HEADER) {
                        storedStack.remove(ParsingState.TASKS_TABLE_HEADER);
                        storedStack.push(ParsingState.TASKS_TABLE_INSIDE);
                    }
                    return LineContinueType.TASK_TABLE_ELEMENT;

                } else if (posResolver.isCorrectPosition(PositionExpected.LINE_CONTINUE_INLINED_FOR_TASK_TABLE,
                        currentLine, currentToken)) {
                    return LineContinueType.LINE_CONTINUE_INLINED;
                }
            } else if (currentState == ParsingState.KEYWORD_TABLE_INSIDE
                    || currentState == ParsingState.KEYWORD_DECLARATION) {

                if (isNewLine && posResolver.isCorrectPosition(PositionExpected.LINE_CONTINUE_NEWLINE_FOR_KEYWORD_TABLE,
                        currentLine, currentToken)) {
                    final ParsingState state = stateHelper.getCurrentState(storedStack);
                    if (state == ParsingState.KEYWORD_TABLE_HEADER) {
                        storedStack.remove(ParsingState.KEYWORD_TABLE_HEADER);
                        storedStack.push(ParsingState.KEYWORD_TABLE_INSIDE);
                    }
                    return LineContinueType.KEYWORD_TABLE_ELEMENT;

                } else if (posResolver.isCorrectPosition(PositionExpected.LINE_CONTINUE_INLINED_FOR_KEYWORD_TABLE,
                        currentLine, currentToken)) {
                    return LineContinueType.LINE_CONTINUE_INLINED;
                }
            } else if (currentState == ParsingState.TEST_CASE_INSIDE_ACTION
                    || currentState == ParsingState.KEYWORD_INSIDE_ACTION) {

                if (posResolver.isCorrectPosition(PositionExpected.LINE_CONTINUE_INLINED_IN_FOR_LOOP, currentLine,
                        currentToken)) {
                    return LineContinueType.LINE_CONTINUE_INLINED;
                }
            }
        }
        return LineContinueType.NONE;
    }

    private boolean isPreviousLineContinueToken(final RobotToken currentToken) {
        if (currentToken.getTypes().size() == 1
                && currentToken.getTypes().contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)) {
            return true;
        } else {
            final boolean isLineContinuation = currentToken.getText().matches("^( )?[.]{3}$");
            if (isLineContinuation && !currentToken.getTypes().contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)) {
                currentToken.getTypes().add(RobotTokenType.PREVIOUS_LINE_CONTINUE);
            }
            return isLineContinuation;
        }

    }

    private boolean isCommentContinue(final RobotToken currentToken, final Stack<ParsingState> storedStack) {
        return currentToken.getTypes().contains(RobotTokenType.START_HASH_COMMENT) && !storedStack.isEmpty()
                && storedStack.get(storedStack.size() - 1) == ParsingState.COMMENT;
    }

    public boolean isSomethingToContinue(final RobotFile model) {
        final List<RobotLine> fileContent = model.getFileContent();
        for (int i = fileContent.size() - 1; i > 0; i--) {
            final List<IRobotLineElement> lineElements = fileContent.get(i).getLineElements();
            if (!lineElements.isEmpty()) {
                for (int k = 0; k < lineElements.size() && k < 2; k++) {
                    final IRobotLineElement elem = lineElements.get(k);
                    final List<IRobotTokenType> types = elem.getTypes();

                    if (types.contains(SeparatorType.PIPE) || types.contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE)
                            || types.contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)
                            || types.contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                        continue;

                    }
                    return !(types.contains(RobotTokenType.KEYWORDS_TABLE_HEADER)
                            || types.contains(RobotTokenType.SETTINGS_TABLE_HEADER)
                            || types.contains(RobotTokenType.VARIABLES_TABLE_HEADER)
                            || types.contains(RobotTokenType.TEST_CASES_TABLE_HEADER)
                            || types.contains(RobotTokenType.TASKS_TABLE_HEADER));
                }
            }
        }
        return false;
    }

    private boolean containsAnySetting(final RobotFile file) {
        return !file.getSettingTable().isEmpty();
    }

    private boolean containsAnyVariable(final RobotFile file) {
        return !file.getVariableTable().getVariables().isEmpty();
    }

    public boolean isSomethingToDo(final LineContinueType type) {
        return type != LineContinueType.NONE;
    }

    public void restorePreviousStack(final Stack<ParsingState> parsingStates) {
        parsingStates.clear();
        removeLastNotWantedStates(storedStack);
        parsingStates.addAll(storedStack);
    }

    private void removeLastNotWantedStates(final Stack<ParsingState> parsingStates) {
        for (int i = parsingStates.size() - 1; i >= 0; i--) {
            final ParsingState state = parsingStates.get(i);
            if (state == ParsingState.COMMENT || state == ParsingState.TEST_CASE_EMPTY_LINE
                    || state == ParsingState.TASK_EMPTY_LINE || state == ParsingState.KEYWORD_EMPTY_LINE) {
                parsingStates.remove(i);
            } else {
                break;
            }
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
        NONE,
        SETTING_TABLE_ELEMENT,
        VARIABLE_TABLE_ELEMENT,
        TEST_CASE_TABLE_ELEMENT,
        TASK_TABLE_ELEMENT,
        KEYWORD_TABLE_ELEMENT,
        LINE_CONTINUE_INLINED;
    }
}
