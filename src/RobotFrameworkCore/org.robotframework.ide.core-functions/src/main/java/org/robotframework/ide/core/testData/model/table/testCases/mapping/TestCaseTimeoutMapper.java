/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.testCases.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTimeout;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestCaseTimeoutMapper extends ATestCaseSettingDeclarationMapper {

    public TestCaseTimeoutMapper() {
        super(RobotTokenType.TEST_CASE_SETTING_TIMEOUT);
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.TEST_CASE_SETTING_TIMEOUT);

        rt.setRaw(new StringBuilder(text));
        rt.setText(new StringBuilder(text));

        TestCase testCase = finder.findOrCreateNearestTestCase(currentLine,
                processingState, robotFileOutput, rt, fp);
        if (testCase.getTimeouts().isEmpty()) {
            TestCaseTimeout timeout = new TestCaseTimeout(rt);
            testCase.addTimeout(timeout);
        }
        processingState.push(ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT);

        return rt;
    }
}
