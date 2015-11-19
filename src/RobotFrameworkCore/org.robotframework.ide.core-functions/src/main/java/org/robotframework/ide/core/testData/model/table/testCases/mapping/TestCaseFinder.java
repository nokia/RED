/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.testCases.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestCaseFinder {

    public TestCase findOrCreateNearestTestCase(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp) {
        final RobotFile fileModel = robotFileOutput.getFileModel();
        final TestCaseTable testCaseTable = fileModel.getTestCaseTable();

        TestCase testCase;
        final List<TestCase> lastHeaderTestCases = filterByTestCasesAfterLastHeader(testCaseTable);
        if (lastHeaderTestCases.isEmpty()) {
            testCase = createArtificialTestCase(robotFileOutput, testCaseTable);
            testCaseTable.addTest(testCase);

            final RobotLine lineToModify = findRobotLineInModel(fileModel, testCase,
                    currentLine);
            lineToModify.addLineElementAt(0, testCase.getTestName());
        } else {
            testCase = lastHeaderTestCases.get(lastHeaderTestCases.size() - 1);
        }

        return testCase;
    }


    private RobotLine findRobotLineInModel(final RobotFile fileModel,
            final TestCase testCase, final RobotLine currentLine) {
        RobotLine foundLine = currentLine;
        if (currentLine.getLineNumber() != testCase.getBeginPosition()
                .getLine()) {
            final List<RobotLine> fileContent = fileModel.getFileContent();
            for (final RobotLine line : fileContent) {
                if (testCase.getBeginPosition().getLine() == line
                        .getLineNumber()) {
                    foundLine = line;
                    break;
                }
            }
        }

        return foundLine;
    }


    private TestCase createArtificialTestCase(final RobotFileOutput robotFileOutput,
            final TestCaseTable testCaseTable) {
        TestCase testCase;
        final List<TableHeader<? extends ARobotSectionTable>> headers = testCaseTable
                .getHeaders();
        final TableHeader<?> tableHeader = headers.get(headers.size() - 1);
        final RobotToken artificialNameToken = new RobotToken();
        artificialNameToken.setLineNumber(tableHeader.getTableHeader()
                .getLineNumber() + 1);
        artificialNameToken.setRaw("");
        artificialNameToken.setText("");
        artificialNameToken.setStartColumn(0);
        final RobotLine robotLine = robotFileOutput.getFileModel().getFileContent()
                .get(tableHeader.getTableHeader().getLineNumber() - 1);
        final IRobotLineElement endOfLine = robotLine.getEndOfLine();
        artificialNameToken.setStartOffset(endOfLine.getStartOffset()
                + endOfLine.getRaw().length());
        artificialNameToken.setType(RobotTokenType.TEST_CASE_NAME);

        testCase = new TestCase(artificialNameToken);
        return testCase;
    }


    public List<TestCase> filterByTestCasesAfterLastHeader(
            final TestCaseTable testCaseTable) {
        final List<TestCase> testCases = new ArrayList<>();

        final List<TableHeader<? extends ARobotSectionTable>> headers = testCaseTable
                .getHeaders();
        if (!headers.isEmpty()) {
            final List<TestCase> testCasesAvail = testCaseTable.getTestCases();
            final TableHeader<?> tableHeader = headers.get(headers.size() - 1);
            final int tableHeaderLineNumber = tableHeader.getTableHeader()
                    .getLineNumber();
            final int numberOfTestCases = testCasesAvail.size();
            for (int i = numberOfTestCases - 1; i >= 0; i--) {
                final TestCase test = testCasesAvail.get(i);
                if (test.getTestName().getLineNumber() > tableHeaderLineNumber) {
                    testCases.add(test);
                }
            }

            Collections.reverse(testCases);
        }

        return testCases;
    }
}
