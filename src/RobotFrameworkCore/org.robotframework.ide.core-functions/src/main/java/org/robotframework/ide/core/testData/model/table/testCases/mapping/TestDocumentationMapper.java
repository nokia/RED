package org.robotframework.ide.core.testData.model.table.testCases.mapping;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestDocumentation;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class TestDocumentationMapper implements IParsingMapper {

    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION);
        rt.setText(new StringBuilder(text));

        TestCase testCase = findOrCreateNearestTestCase(currentLine,
                processingState, robotFileOutput, rt, fp);
        TestDocumentation doc = new TestDocumentation(rt);
        testCase.addDocumentation(doc);

        processingState
                .push(ParsingState.TEST_CASE_SETTING_DOCUMENTATION_DECLARATION);

        return rt;
    }


    @VisibleForTesting
    protected TestCase findOrCreateNearestTestCase(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp) {
        TestCaseTable testCaseTable = robotFileOutput.getFileModel()
                .getTestCaseTable();

        TestCase testCase;
        List<TestCase> lastHeaderTestCases = filterByTestCasesAfterLastHeader(testCaseTable);
        if (lastHeaderTestCases.isEmpty()) {
            testCase = createArtificialTestCase(testCaseTable);
            testCaseTable.addTest(testCase);
        } else {
            testCase = lastHeaderTestCases.get(lastHeaderTestCases.size() - 1);
        }

        return testCase;
    }


    private TestCase createArtificialTestCase(TestCaseTable testCaseTable) {
        TestCase testCase;
        List<TableHeader> headers = testCaseTable.getHeaders();
        TableHeader tableHeader = headers.get(headers.size() - 1);
        RobotToken artificialNameToken = new RobotToken();
        artificialNameToken.setLineNumber(tableHeader.getTableHeader()
                .getLineNumber() + 1);
        artificialNameToken.setRaw(new StringBuilder());
        artificialNameToken.setText(new StringBuilder());
        artificialNameToken.setStartColumn(0);
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


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;

        if (rt.getTypes().get(0) == RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION) {
            List<IRobotLineElement> lineElements = currentLine
                    .getLineElements();
            int size = lineElements.size();
            if (size == 1) {
                List<IRobotTokenType> types = lineElements.get(0).getTypes();
                result = (types.contains(SeparatorType.PIPE) || types
                        .contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE));
            } else {
                for (IRobotLineElement elem : lineElements) {
                    List<IRobotTokenType> types = elem.getTypes();
                    if (types.contains(SeparatorType.PIPE)
                            || types.contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE)) {
                        continue;
                    } else if (types.contains(RobotTokenType.TEST_CASE_NAME)) {
                        result = true;
                        break;
                    } else {
                        result = false;
                        break;
                    }
                }
            }
        }

        return result;
    }

}
