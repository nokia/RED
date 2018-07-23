/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Set;

import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;

/**
 * @author mmarzec
 */
public class ExecutableLineChecker {

    private static Set<RobotTokenType> executableTypes = newHashSet(
            RobotTokenType.KEYWORD_ACTION_NAME,
            RobotTokenType.TEST_CASE_ACTION_NAME,
            RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_NAME,
            RobotTokenType.SETTING_SUITE_TEARDOWN_KEYWORD_NAME,
            RobotTokenType.SETTING_TEST_SETUP_KEYWORD_NAME,
            RobotTokenType.SETTING_TEST_TEARDOWN_KEYWORD_NAME,
            RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_NAME,
            RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_NAME);

    public static boolean isExecutableLine(final RobotFile file, final int lineNumber) {
        final int zeroBasedLineNumber = lineNumber - 1;

        if (file == null || zeroBasedLineNumber < 0 || zeroBasedLineNumber >= file.getFileContent().size()) {
            return false;
        }

        final RobotLine robotLine = file.getFileContent().get(zeroBasedLineNumber);
        final List<IRobotLineElement> lineElements = robotLine.getLineElements();
        if (hasComment(lineElements)) {
            return false;
        }
        for (final IRobotLineElement robotLineElement : lineElements) {
            if (hasExecutableRobotLineType(robotLineElement.getTypes())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasExecutableRobotLineType(final List<IRobotTokenType> types) {
        return !hasSeparator(types) && hasExecutableType(types);
    }

    public static boolean hasExecutableType(final List<IRobotTokenType> types) {
        return types.stream().anyMatch(executableTypes::contains);
    }

    private static boolean hasSeparator(final List<IRobotTokenType> types) {
        return types.contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE) || types.contains(SeparatorType.PIPE);
    }

    private static boolean hasComment(final List<IRobotLineElement> lineElements) {
        if (lineElements.isEmpty()) {
            return false;
        }
        final List<IRobotTokenType> types = lineElements.get(0).getTypes();
        final List<IRobotTokenType> typesToCheck = hasSeparator(types) && lineElements.size() > 1
                ? lineElements.get(1).getTypes()
                : types;
        return hasCommentTypes(typesToCheck);
    }

    private static boolean hasCommentTypes(final List<IRobotTokenType> types) {
        return types.contains(RobotTokenType.START_HASH_COMMENT) || types.contains(RobotTokenType.COMMENT_CONTINUE);
    }
}
