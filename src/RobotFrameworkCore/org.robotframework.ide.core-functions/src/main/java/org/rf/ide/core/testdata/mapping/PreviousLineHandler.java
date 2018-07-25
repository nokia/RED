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

import com.google.common.annotations.VisibleForTesting;

@SuppressWarnings("PMD.GodClass")
public class PreviousLineHandler {

    private final ElementPositionResolver posResolver;

    private final ParsingStateHelper stateHelper;

    public PreviousLineHandler() {
        this.posResolver = new ElementPositionResolver();
        this.stateHelper = new ParsingStateHelper();
    }

    private final Stack<ParsingState> storedStack = new Stack<>();

    public LineContinueType computeLineContinue(final Stack<ParsingState> parsingStates, final boolean isNewLine,
            final RobotFile model, final RobotLine currentLine, final RobotToken currentToken) {
        LineContinueType continueType = LineContinueType.NONE;

        if (isPreviousLineContinueToken(currentLine, currentToken) || isCommentContinue(currentToken, storedStack)) {
            final ParsingState currentState = stateHelper.getCurrentStatus(parsingStates);

            if (currentState == ParsingState.SETTING_TABLE_INSIDE) {
                if (isNewLine && containsAnySetting(model) && isSomethingToContinue(model)) {
                    if (posResolver.isCorrectPosition(PositionExpected.LINE_CONTINUE_NEWLINE_FOR_SETTING_TABLE, model,
                            currentLine, currentToken)) {
                        continueType = LineContinueType.SETTING_TABLE_ELEMENT;
                    }
                }
            } else if (currentState == ParsingState.VARIABLE_TABLE_INSIDE) {
                if (isNewLine && containsAnyVariable(model) && isSomethingToContinue(model)) {
                    if (posResolver.isCorrectPosition(PositionExpected.LINE_CONTINUE_NEWLINE_FOR_VARIABLE_TABLE, model,
                            currentLine, currentToken)) {
                        continueType = LineContinueType.VARIABLE_TABLE_ELEMENT;
                    }
                }
            } else if (currentState == ParsingState.TEST_CASE_TABLE_INSIDE
                    || currentState == ParsingState.TEST_CASE_DECLARATION) {
                if (isNewLine) {
                    if (posResolver.isCorrectPosition(PositionExpected.LINE_CONTINUE_NEWLINE_FOR_TESTCASE_TABLE, model,
                            currentLine, currentToken)) {
                        final ParsingState state = stateHelper.getCurrentStatus(storedStack);
                        if (state == ParsingState.TEST_CASE_TABLE_HEADER) {
                            storedStack.remove(ParsingState.TEST_CASE_TABLE_HEADER);
                            storedStack.push(ParsingState.TEST_CASE_TABLE_INSIDE);
                        }
                        continueType = LineContinueType.TEST_CASE_TABLE_ELEMENT;
                    }
                } else {
                    if (posResolver.isCorrectPosition(PositionExpected.LINE_CONTINUE_INLINED_FOR_TESTCASE_TABLE, model,
                            currentLine, currentToken)) {
                        continueType = LineContinueType.LINE_CONTINUE_INLINED;
                    }
                }
            } else if (currentState == ParsingState.KEYWORD_TABLE_INSIDE
                    || currentState == ParsingState.KEYWORD_DECLARATION) {
                if (isNewLine) {
                    if (posResolver.isCorrectPosition(PositionExpected.LINE_CONTINUE_NEWLINE_FOR_KEYWORD_TABLE, model,
                            currentLine, currentToken)) {
                        final ParsingState state = stateHelper.getCurrentStatus(storedStack);
                        if (state == ParsingState.KEYWORD_TABLE_HEADER) {
                            storedStack.remove(ParsingState.KEYWORD_TABLE_HEADER);
                            storedStack.push(ParsingState.KEYWORD_TABLE_INSIDE);
                        }
                        continueType = LineContinueType.KEYWORD_TABLE_ELEMENT;
                    }
                } else {
                    if (posResolver.isCorrectPosition(PositionExpected.LINE_CONTINUE_INLINED_FOR_KEYWORD_TABLE, model,
                            currentLine, currentToken)) {
                        continueType = LineContinueType.LINE_CONTINUE_INLINED;
                    }
                }
            } else if (currentState == ParsingState.TEST_CASE_INSIDE_ACTION
                    || currentState == ParsingState.KEYWORD_INSIDE_ACTION) {
                if (posResolver.isCorrectPosition(PositionExpected.LINE_CONTINUE_INLINED_IN_FOR_LOOP, model,
                        currentLine, currentToken)) {
                    continueType = LineContinueType.LINE_CONTINUE_INLINED;
                }
            }
        }

        return continueType;
    }

    @VisibleForTesting
    protected boolean isPreviousLineContinueToken(final RobotLine currentLine, final RobotToken currentToken) {
        boolean result = false;
        if (currentToken.getTypes().size() == 1
                && currentToken.getTypes().contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)) {
            result = true;
        } else {
            result = currentToken.getText().matches("^( )?[.]{3}$");
            if (result && !currentToken.getTypes().contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)) {
                currentToken.getTypes().add(RobotTokenType.PREVIOUS_LINE_CONTINUE);
            }
        }

        return result;
    }

    @VisibleForTesting
    protected boolean isCommentContinue(final RobotToken currentToken, final Stack<ParsingState> storedStack) {
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
        final List<RobotLine> fileContent = model.getFileContent();
        boolean notFoundYet = true;
        for (int i = fileContent.size() - 1; i > 0 && notFoundYet; i--) {
            final RobotLine robotLine = fileContent.get(i);
            final List<IRobotLineElement> lineElements = robotLine.getLineElements();
            if (!lineElements.isEmpty()) {
                for (int k = 0; k < lineElements.size() && k < 2; k++) {
                    final IRobotLineElement elem = lineElements.get(k);
                    final List<IRobotTokenType> types = elem.getTypes();
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

    public void restorePreviousStack(final Stack<ParsingState> parsingStates) {
        parsingStates.clear();
        removeLastNotWantedStates(storedStack);
        parsingStates.addAll(storedStack);
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
        KEYWORD_TABLE_ELEMENT,
        LINE_CONTINUE_INLINED;
    }

    @VisibleForTesting
    protected void removeLastNotWantedStates(final Stack<ParsingState> parsingStates) {
        for (int i = parsingStates.size() - 1; i >= 0; i--) {
            final ParsingState state = parsingStates.get(i);
            if (state == ParsingState.COMMENT || state == ParsingState.TEST_CASE_EMPTY_LINE
                    || state == ParsingState.KEYWORD_EMPTY_LINE) {
                parsingStates.remove(i);
            } else {
                break;
            }
        }
    }
}
