package org.robotframework.ide.core.testData.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestSetupRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?(("
            + createUpperLowerCaseWord("Test") + "[\\s]+"
            + createUpperLowerCaseWord("Setup") + "[\\s]*:" + "|"
            + createUpperLowerCaseWord("Test") + "[\\s]+"
            + createUpperLowerCaseWord("Setup") + ")|("
            + createUpperLowerCaseWord("Test") + "[\\s]+"
            + createUpperLowerCaseWord("Precondition") + "[\\s]*:" + "|"
            + createUpperLowerCaseWord("Test") + "[\\s]+"
            + createUpperLowerCaseWord("Precondition") + "))");


    public TestSetupRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_TEST_SETUP_DECLARATION);
    }
}
