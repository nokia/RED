/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.execution.context;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

/**
 * @author mmarzec
 */
public class RobotDebugExecutableLineChecker {

    private static RobotTokenType[] executableTypes = new RobotTokenType[] { 
            RobotTokenType.KEYWORD_ACTION_NAME,
            RobotTokenType.TEST_CASE_ACTION_NAME, 
            RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_NAME,
            RobotTokenType.SETTING_SUITE_TEARDOWN_KEYWORD_NAME, 
            RobotTokenType.SETTING_TEST_SETUP_KEYWORD_NAME,
            RobotTokenType.SETTING_TEST_TEARDOWN_KEYWORD_NAME, 
            RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_NAME,
            RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_NAME 
    };

    private RobotDebugExecutableLineChecker() {
    }

    public static boolean isExecutableLine(final RobotFile file, final int lineNumber) {
        if (file != null && (lineNumber - 1) >= 0 && (lineNumber-1) < file.getFileContent().size()) {
            final RobotLine robotLine = file.getFileContent().get(lineNumber - 1);
            final List<IRobotLineElement> lineElements = robotLine.getLineElements();
            if(hasComment(lineElements)) {
                return false;
            }
            for (IRobotLineElement robotLineElement : lineElements) {
                if (hasExecutableRobotLineType(robotLineElement.getTypes())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasExecutableRobotLineType(final List<IRobotTokenType> types) {
        return !hasSeparator(types) && hasExecutableType(types);
    }

    private static boolean hasExecutableType(final List<IRobotTokenType> types) {
        for (int i = 0; i < executableTypes.length; i++) {
            if (types.contains(executableTypes[i])) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSeparator(final List<IRobotTokenType> types) {
        return types.contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE) || types.contains(SeparatorType.PIPE);
    }
    
    private static boolean hasComment(final List<IRobotLineElement> lineElements) {
        if (!lineElements.isEmpty()) {
            final List<IRobotTokenType> types = lineElements.get(0).getTypes();
            if(hasSeparator(types) && lineElements.size() > 1) {
                return hasCommentTypes(lineElements.get(1).getTypes());
            } else {
                return hasCommentTypes(types);
            }
        }
        return false;
    }
    
    private static boolean hasCommentTypes(final List<IRobotTokenType> types) {
        return types.contains(RobotTokenType.START_HASH_COMMENT) || types.contains(RobotTokenType.COMMENT_CONTINUE);
    }
}
