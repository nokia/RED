/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.testcases;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseFinder {

    public TestCase findOrCreateNearestTestCase(final RobotLine currentLine,
            final RobotFileOutput robotFileOutput) {
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
        artificialNameToken.setText("");
        artificialNameToken.setStartColumn(0);
        final RobotLine robotLine = robotFileOutput.getFileModel().getFileContent()
                .get(tableHeader.getTableHeader().getLineNumber() - 1);
        final IRobotLineElement endOfLine = robotLine.getEndOfLine();
        artificialNameToken.setStartOffset(endOfLine.getStartOffset()
                + endOfLine.getText().length());
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
            for (int i = 0; i < numberOfTestCases; i++) {
                final TestCase test = testCasesAvail.get(i);
                if (test.getTestName().getLineNumber() > tableHeaderLineNumber) {
                    testCases.add(test);
                }
            }
        }

        return testCases;
    }
}
