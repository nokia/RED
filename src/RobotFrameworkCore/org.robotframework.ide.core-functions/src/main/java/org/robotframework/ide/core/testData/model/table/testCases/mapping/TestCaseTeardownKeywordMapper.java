package org.robotframework.ide.core.testData.model.table.testCases.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.IRobotFileOutput;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTeardown;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestCaseTeardownKeywordMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public TestCaseTeardownKeywordMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            IRobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_NAME);

        rt.setRaw(new StringBuilder(text));
        rt.setText(new StringBuilder(text));
        List<TestCase> testCases = robotFileOutput.getFileModel()
                .getTestCaseTable().getTestCases();
        TestCase testCase = testCases.get(testCases.size() - 1);
        List<TestCaseTeardown> teardowns = testCase.getTeardowns();

        TestCaseTeardown testTeardown = teardowns.get(teardowns.size() - 1);
        testTeardown.setKeywordName(rt);

        processingState.push(ParsingState.TEST_CASE_SETTING_TEARDOWN_KEYWORD);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(IRobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        ParsingState state = utility.getCurrentStatus(processingState);
        if (state == ParsingState.TEST_CASE_SETTING_TEARDOWN) {
            List<TestCase> tests = robotFileOutput.getFileModel()
                    .getTestCaseTable().getTestCases();
            List<TestCaseTeardown> teardowns = tests.get(tests.size() - 1)
                    .getTeardowns();
            result = !utility.checkIfHasAlreadyKeywordName(teardowns);
        }

        return result;
    }

}
