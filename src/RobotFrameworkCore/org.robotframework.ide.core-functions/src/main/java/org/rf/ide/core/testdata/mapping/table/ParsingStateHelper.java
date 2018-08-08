/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class ParsingStateHelper {

    boolean isTypeForState(final ParsingState state, final RobotToken token) {

        List<RobotTokenType> typesForState = new ArrayList<>();
        if (state == ParsingState.TEST_CASE_TABLE_INSIDE || state == ParsingState.TEST_CASE_DECLARATION) {
            typesForState = RobotTokenType.getTypesForTestCasesTable();

        } else if (state == ParsingState.TASKS_TABLE_INSIDE || state == ParsingState.TASK_DECLARATION) {
            typesForState = RobotTokenType.getTypesForTasksTable();

        } else if (state == ParsingState.SETTING_TABLE_INSIDE) {
            typesForState = RobotTokenType.getTypesForSettingsTable();

        } else if (state == ParsingState.VARIABLE_TABLE_INSIDE) {
            typesForState = RobotTokenType.getTypesForVariablesTable();

        } else if (state == ParsingState.KEYWORD_TABLE_INSIDE || state == ParsingState.KEYWORD_DECLARATION) {
            typesForState = RobotTokenType.getTypesForKeywordsTable();
        }

        final List<IRobotTokenType> types = token.getTypes();
        for (final IRobotTokenType type : types) {
            if (typesForState.contains(type)) {
                return true;
            }
        }

        return (state == ParsingState.TEST_CASE_DECLARATION || state == ParsingState.TASK_DECLARATION
                || state == ParsingState.KEYWORD_DECLARATION || state == ParsingState.UNKNOWN)
                && (types.contains(RobotTokenType.START_HASH_COMMENT)
                        || types.contains(RobotTokenType.COMMENT_CONTINUE));
    }

    public void updateStatusesForNewLine(final Stack<ParsingState> processingState) {
        while (true) {
            final ParsingState status = getCurrentState(processingState);
            if (isTableHeaderState(status)) {
                processingState.pop();
                if (status == ParsingState.SETTING_TABLE_HEADER) {
                    processingState.push(ParsingState.SETTING_TABLE_INSIDE);
                } else if (status == ParsingState.VARIABLE_TABLE_HEADER) {
                    processingState.push(ParsingState.VARIABLE_TABLE_INSIDE);
                } else if (status == ParsingState.TEST_CASE_TABLE_HEADER) {
                    processingState.push(ParsingState.TEST_CASE_TABLE_INSIDE);
                } else if (status == ParsingState.TASKS_TABLE_HEADER) {
                    processingState.push(ParsingState.TASKS_TABLE_INSIDE);
                } else if (status == ParsingState.KEYWORD_TABLE_HEADER) {
                    processingState.push(ParsingState.KEYWORD_TABLE_INSIDE);
                }
                break;

            } else if (isTableInsideState(status) || isKeywordExecution(status) || isTestCaseExecution(status)
                    || isTaskExecution(status)) {
                break;
            } else if (!processingState.isEmpty()) {
                processingState.pop();
            } else {
                break;
            }
        }
    }

    private boolean isKeywordExecution(final ParsingState status) {
        return status == ParsingState.KEYWORD_DECLARATION;
    }

    private boolean isTestCaseExecution(final ParsingState status) {
        return status == ParsingState.TEST_CASE_DECLARATION;
    }

    private boolean isTaskExecution(final ParsingState status) {
        return status == ParsingState.TASK_DECLARATION;
    }

    public boolean isTableHeaderState(final ParsingState state) {
        return state == ParsingState.TEST_CASE_TABLE_HEADER || state == ParsingState.TASKS_TABLE_HEADER
                || state == ParsingState.SETTING_TABLE_HEADER || state == ParsingState.VARIABLE_TABLE_HEADER
                || state == ParsingState.KEYWORD_TABLE_HEADER;
    }

    boolean isTableInsideStateInHierarchy(final ParsingState state) {
        ParsingState currentState = state;
        while (currentState != null) {
            if (isTableInsideState(currentState)) {
                return true;
            }
            currentState = currentState.getPreviousState();
        }
        return false;
    }

    boolean isTableInsideState(final ParsingState state) {
        return state == ParsingState.SETTING_TABLE_INSIDE || state == ParsingState.TEST_CASE_TABLE_INSIDE
                || state == ParsingState.TASKS_TABLE_INSIDE || state == ParsingState.KEYWORD_TABLE_INSIDE
                || state == ParsingState.VARIABLE_TABLE_INSIDE;
    }

    public ParsingState getCurrentState(final Stack<ParsingState> processingState) {
        return processingState.isEmpty() ? ParsingState.UNKNOWN : processingState.peek();
    }

    public ParsingState getState(final RobotToken token) {
        final List<IRobotTokenType> types = token.getTypes();
        if (types.contains(RobotTokenType.SETTINGS_TABLE_HEADER)) {
            return ParsingState.SETTING_TABLE_HEADER;

        } else if (types.contains(RobotTokenType.VARIABLES_TABLE_HEADER)) {
            return ParsingState.VARIABLE_TABLE_HEADER;

        } else if (types.contains(RobotTokenType.TEST_CASES_TABLE_HEADER)) {
            return ParsingState.TEST_CASE_TABLE_HEADER;

        } else if (types.contains(RobotTokenType.TASKS_TABLE_HEADER)) {
            return ParsingState.TASKS_TABLE_HEADER;

        } else if (types.contains(RobotTokenType.KEYWORDS_TABLE_HEADER)) {
            return ParsingState.KEYWORD_TABLE_HEADER;
        }
        return ParsingState.UNKNOWN;
    }

    ParsingState getFirstTableHeaderState(final Stack<ParsingState> processingState) {
        return processingState.stream().filter(this::isTableHeaderState).findFirst().orElse(ParsingState.UNKNOWN);
    }

    public ParsingState getLastNotCommentState(final Stack<ParsingState> processingState) {
        return processingState.stream()
                .filter(state -> state != ParsingState.COMMENT)
                .reduce((fst, snd) -> snd)
                .orElse(ParsingState.UNKNOWN);
    }
}
