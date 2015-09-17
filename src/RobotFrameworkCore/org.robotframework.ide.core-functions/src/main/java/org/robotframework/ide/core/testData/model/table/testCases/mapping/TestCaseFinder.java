/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.testCases.mapping;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class TestCaseFinder {

    public TestCase findOrCreateNearestTestCase(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp) {
        TestCaseTable testCaseTable = robotFileOutput.getFileModel()
                .getTestCaseTable();

        TestCase testCase;
        List<TestCase> lastHeaderTestCases = filterByTestCasesAfterLastHeader(testCaseTable);
        if (lastHeaderTestCases.isEmpty()) {
            testCase = createArtificialTestCase(robotFileOutput, testCaseTable);
            testCaseTable.addTest(testCase);
            currentLine.addLineElementAt(0, testCase.getTestName());
        } else {
            testCase = lastHeaderTestCases.get(lastHeaderTestCases.size() - 1);
        }

        return testCase;
    }


    private TestCase createArtificialTestCase(RobotFileOutput robotFileOutput,
            TestCaseTable testCaseTable) {
        TestCase testCase;
        List<TableHeader> headers = testCaseTable.getHeaders();
        TableHeader tableHeader = headers.get(headers.size() - 1);
        RobotToken artificialNameToken = new RobotToken();
        artificialNameToken.setLineNumber(tableHeader.getTableHeader()
                .getLineNumber() + 1);
        artificialNameToken.setRaw(new StringBuilder());
        artificialNameToken.setText(new StringBuilder());
        artificialNameToken.setStartColumn(0);
        RobotLine robotLine = robotFileOutput.getFileModel().getFileContent()
                .get(tableHeader.getTableHeader().getLineNumber() - 1);
        IRobotLineElement endOfLine = robotLine.getEndOfLine();
        artificialNameToken.setStartOffset(endOfLine.getStartOffset()
                + endOfLine.getRaw().length());
        artificialNameToken.setType(RobotTokenType.TEST_CASE_NAME);

        testCase = new TestCase(artificialNameToken);
        return testCase;
    }


    @VisibleForTesting
    protected List<TestCase> filterByTestCasesAfterLastHeader(
            final TestCaseTable testCaseTable) {
        List<TestCase> testCases = new LinkedList<>();

        List<TableHeader> headers = testCaseTable.getHeaders();
        if (!headers.isEmpty()) {
            List<TestCase> testCasesAvail = testCaseTable.getTestCases();
            TableHeader tableHeader = headers.get(headers.size() - 1);
            int tableHeaderLineNumber = tableHeader.getTableHeader()
                    .getLineNumber();
            int numberOfTestCases = testCasesAvail.size();
            for (int i = numberOfTestCases - 1; i >= 0; i--) {
                TestCase test = testCasesAvail.get(i);
                if (test.getTestName().getLineNumber() > tableHeaderLineNumber) {
                    testCases.add(test);
                }
            }

            Collections.reverse(testCases);
        }

        return testCases;
    }
}
