/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.mapping;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class ParsingStateHelper {

    public boolean isTypeForState(final ParsingState state, final RobotToken rt) {
        RobotTokenType robotType = RobotTokenType.UNKNOWN;
        boolean result = false;

        List<RobotTokenType> typesForState = new LinkedList<>();
        if (state == ParsingState.TEST_CASE_TABLE_INSIDE
                || state == ParsingState.TEST_CASE_DECLARATION) {
            typesForState = robotType.getTypesForTestCasesTable();
        } else if (state == ParsingState.SETTING_TABLE_INSIDE) {
            typesForState = robotType.getTypesForSettingsTable();
        } else if (state == ParsingState.VARIABLE_TABLE_INSIDE) {
            typesForState = robotType.getTypesForVariablesTable();
        } else if (state == ParsingState.KEYWORD_TABLE_INSIDE
                || state == ParsingState.KEYWORD_DECLARATION) {
            typesForState = robotType.getTypesForKeywordsTable();
        }

        List<IRobotTokenType> types = rt.getTypes();
        for (IRobotTokenType type : types) {
            if (typesForState.contains(type)) {
                result = true;
                break;
            }
        }

        if (!result) {
            if (state == ParsingState.TEST_CASE_DECLARATION
                    || state == ParsingState.KEYWORD_DECLARATION
                    || state == ParsingState.UNKNOWN) {
                result = (types.contains(RobotTokenType.START_HASH_COMMENT) || types
                        .contains(RobotTokenType.COMMENT_CONTINUE));

            }
        }

        return result;
    }


    public ParsingState findNearestNotCommentState(
            final Stack<ParsingState> processingState) {
        ParsingState state = ParsingState.UNKNOWN;
        for (ParsingState s : processingState) {
            if (s != ParsingState.COMMENT) {
                state = s;
            }
        }
        return state;
    }


    public void updateStatusesForNewLine(
            final Stack<ParsingState> processingState) {

        boolean clean = true;
        while(clean) {
            ParsingState status = getCurrentStatus(processingState);
            if (isTableHeader(status)) {
                processingState.pop();
                if (status == ParsingState.SETTING_TABLE_HEADER) {
                    processingState.push(ParsingState.SETTING_TABLE_INSIDE);
                } else if (status == ParsingState.VARIABLE_TABLE_HEADER) {
                    processingState.push(ParsingState.VARIABLE_TABLE_INSIDE);
                } else if (status == ParsingState.TEST_CASE_TABLE_HEADER) {
                    processingState.push(ParsingState.TEST_CASE_TABLE_INSIDE);
                } else if (status == ParsingState.KEYWORD_TABLE_HEADER) {
                    processingState.push(ParsingState.KEYWORD_TABLE_INSIDE);
                }

                clean = false;
            } else if (isTableInsideState(status)) {
                clean = false;
            } else if (isKeywordExecution(status)) {
                clean = false;
            } else if (isTestCaseExecution(status)) {
                clean = false;
            } else if (!processingState.isEmpty()) {
                processingState.pop();
            } else {
                clean = false;
            }
        }
    }


    public boolean isTestCaseExecution(ParsingState status) {
        return (status == ParsingState.TEST_CASE_DECLARATION);
    }


    public boolean isKeywordExecution(ParsingState status) {
        return (status == ParsingState.KEYWORD_DECLARATION);
    }


    public boolean isTableInsideStateInHierarchy(ParsingState state) {
        boolean result = false;
        if (!isTableInsideState(state)) {
            ParsingState parent = null;
            while((parent = state.getPreviousState()) != null) {
                if (isTableInsideState(parent)) {
                    result = true;
                    break;
                } else {
                    state = parent;
                }
            }
        } else {
            result = true;
        }

        return result;
    }


    public boolean isTableState(ParsingState state) {
        return state == ParsingState.TEST_CASE_TABLE_HEADER
                || state == ParsingState.SETTING_TABLE_HEADER
                || state == ParsingState.VARIABLE_TABLE_HEADER
                || state == ParsingState.KEYWORD_TABLE_HEADER;
    }


    public boolean isTableInsideState(ParsingState state) {
        return state == ParsingState.SETTING_TABLE_INSIDE
                || state == ParsingState.TEST_CASE_TABLE_INSIDE
                || state == ParsingState.KEYWORD_TABLE_INSIDE
                || state == ParsingState.VARIABLE_TABLE_INSIDE;
    }


    public boolean isTableHeader(ParsingState state) {
        boolean result = false;
        if (state == ParsingState.SETTING_TABLE_HEADER
                || state == ParsingState.VARIABLE_TABLE_HEADER
                || state == ParsingState.TEST_CASE_TABLE_HEADER
                || state == ParsingState.KEYWORD_TABLE_HEADER) {
            result = true;
        }

        return result;
    }


    public ParsingState getCurrentStatus(Stack<ParsingState> processingState) {
        ParsingState state = ParsingState.UNKNOWN;

        if (!processingState.isEmpty()) {
            state = processingState.peek();
        }

        return state;
    }


    public ParsingState getStatus(RobotToken t) {
        ParsingState status = ParsingState.UNKNOWN;
        List<IRobotTokenType> types = t.getTypes();
        if (types.contains(RobotTokenType.SETTINGS_TABLE_HEADER)) {
            status = ParsingState.SETTING_TABLE_HEADER;
        } else if (types.contains(RobotTokenType.VARIABLES_TABLE_HEADER)) {
            status = ParsingState.VARIABLE_TABLE_HEADER;
        } else if (types.contains(RobotTokenType.TEST_CASES_TABLE_HEADER)) {
            status = ParsingState.TEST_CASE_TABLE_HEADER;
        } else if (types.contains(RobotTokenType.KEYWORDS_TABLE_HEADER)) {
            status = ParsingState.KEYWORD_TABLE_HEADER;
        }

        return status;
    }


    public ParsingState getNearestTableHeaderState(
            Stack<ParsingState> processingState) {
        ParsingState state = ParsingState.UNKNOWN;
        for (ParsingState s : processingState) {
            if (isTableState(s)) {
                state = s;
                break;
            }
        }

        return state;
    }
}
