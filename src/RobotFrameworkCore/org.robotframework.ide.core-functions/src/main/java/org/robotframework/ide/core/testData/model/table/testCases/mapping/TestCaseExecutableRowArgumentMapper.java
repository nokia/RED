package org.robotframework.ide.core.testData.model.table.testCases.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.IRobotFileOutput;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestCaseExecutableRowArgumentMapper implements IParsingMapper {

    private final ElementsUtility utility;
    private final TestCaseFinder testCaseFinder;


    public TestCaseExecutableRowArgumentMapper() {
        this.utility = new ElementsUtility();
        this.testCaseFinder = new TestCaseFinder();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            IRobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        TestCase testCase = testCaseFinder.findOrCreateNearestTestCase(
                currentLine, processingState, robotFileOutput, rt, fp);
        List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.TEST_CASE_ACTION_ARGUMENT);

        List<RobotExecutableRow> testExecutionRows = testCase
                .getTestExecutionRows();
        RobotExecutableRow robotExecutableRow = testExecutionRows
                .get(testExecutionRows.size() - 1);
        robotExecutableRow.addArgument(rt);

        processingState.push(ParsingState.TEST_CASE_INSIDE_ACTION_ARGUMENT);
        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(IRobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        ParsingState state = utility.getCurrentStatus(processingState);
        result = (state == ParsingState.TEST_CASE_INSIDE_ACTION || state == ParsingState.TEST_CASE_INSIDE_ACTION_ARGUMENT);

        return result;
    }
}
