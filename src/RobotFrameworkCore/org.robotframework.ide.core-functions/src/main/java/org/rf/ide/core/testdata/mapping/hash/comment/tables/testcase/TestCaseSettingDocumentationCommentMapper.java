/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.hash.comment.tables.testcase;

import java.util.List;

import org.rf.ide.core.testdata.mapping.IHashCommentMapper;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class TestCaseSettingDocumentationCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.TEST_CASE_SETTING_DOCUMENTATION_DECLARATION
                || state == ParsingState.TEST_CASE_SETTING_DOCUMENTATION_TEXT);
    }

    @Override
    public void map(final RobotLine currentLine, final RobotToken rt, final ParsingState currentState,
            final RobotFile fileModel) {
        List<TestCase> testCases = fileModel.getTestCaseTable().getTestCases();
        TestCase testCase = testCases.get(testCases.size() - 1);

        List<TestDocumentation> documentation = testCase.getDocumentation();
        TestDocumentation testDocumentation = documentation.get(documentation.size() - 1);
        testDocumentation.addCommentPart(rt);
    }
}
