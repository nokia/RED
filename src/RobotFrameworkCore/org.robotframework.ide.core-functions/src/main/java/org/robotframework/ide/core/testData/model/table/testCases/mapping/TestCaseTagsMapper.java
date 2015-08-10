package org.robotframework.ide.core.testData.model.table.testCases.mapping;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTags;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestCaseTagsMapper extends ATestCaseSettingDeclarationMapper {

    public TestCaseTagsMapper() {
        super(RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION);
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION);
        rt.setText(new StringBuilder(text));

        TestCase testCase = findOrCreateNearestTestCase(currentLine,
                processingState, robotFileOutput, rt, fp);
        TestCaseTags tags = new TestCaseTags(rt);
        testCase.addTag(tags);

        processingState.push(ParsingState.TEST_CASE_SETTING_TAGS);

        return rt;
    }
}
