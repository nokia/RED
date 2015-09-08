package org.robotframework.ide.core.testData.model.table.testCases.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.IRobotFileOutput;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTags;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestCaseTagsTagNameMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public TestCaseTagsTagNameMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            IRobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.TEST_CASE_SETTING_TAGS);
        rt.setText(new StringBuilder(text));

        TestCaseTable testCaseTable = robotFileOutput.getFileModel()
                .getTestCaseTable();
        List<TestCase> testCases = testCaseTable.getTestCases();
        TestCase testCase = testCases.get(testCases.size() - 1);
        List<TestCaseTags> tags = testCase.getTags();
        TestCaseTags testCaseTags = tags.get(tags.size() - 1);
        testCaseTags.addTag(rt);

        processingState.push(ParsingState.TEST_CASE_SETTING_TAGS_TAG_NAME);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(IRobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        ParsingState state = utility.getCurrentStatus(processingState);
        result = (state == ParsingState.TEST_CASE_SETTING_TAGS || state == ParsingState.TEST_CASE_SETTING_TAGS_TAG_NAME);

        return result;
    }

}
