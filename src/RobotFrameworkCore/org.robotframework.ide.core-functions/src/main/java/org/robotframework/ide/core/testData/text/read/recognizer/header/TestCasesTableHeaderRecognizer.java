package org.robotframework.ide.core.testData.text.read.recognizer.header;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestCasesTableHeaderRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?[*]+[\\s]*"
            + createUpperLowerCaseWord("Test") + "[\\s]+("
            + createUpperLowerCaseWord("Cases") + "|"
            + createUpperLowerCaseWord("Case") + ")[\\s]*[*]*");


    public TestCasesTableHeaderRecognizer() {
        super(EXPECTED, RobotTokenType.TEST_CASES_TABLE_HEADER);
    }
}
