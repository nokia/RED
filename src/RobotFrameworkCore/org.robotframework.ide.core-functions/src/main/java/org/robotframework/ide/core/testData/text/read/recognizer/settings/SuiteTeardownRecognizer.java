package org.robotframework.ide.core.testData.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class SuiteTeardownRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?(("
            + createUpperLowerCaseWord("Suite") + "[\\s]+"
            + createUpperLowerCaseWord("Teardown") + "[\\s]*:" + "|"
            + createUpperLowerCaseWord("Suite") + "[\\s]+"
            + createUpperLowerCaseWord("Teardown") + ")|("
            + createUpperLowerCaseWord("Suite") + "[\\s]+"
            + createUpperLowerCaseWord("Postcondition") + "[\\s]*:" + "|"
            + createUpperLowerCaseWord("Suite") + "[\\s]+"
            + createUpperLowerCaseWord("Postcondition") + "))");


    public SuiteTeardownRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION);
    }
}
