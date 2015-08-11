package org.robotframework.ide.core.testData.model.table.testCases.mapping;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestDocumentation;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestDocumentationMapper extends ATestCaseSettingDeclarationMapper {

    public TestDocumentationMapper() {
        super(RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION);
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION);
        rt.setText(new StringBuilder(text));

        TestCase testCase = finder.findOrCreateNearestTestCase(currentLine,
                processingState, robotFileOutput, rt, fp);
        TestDocumentation doc = new TestDocumentation(rt);
        testCase.addDocumentation(doc);

        processingState
                .push(ParsingState.TEST_CASE_SETTING_DOCUMENTATION_DECLARATION);

        return rt;
    }
}
