package org.robotframework.ide.core.testData.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class VariableDeclarationRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?("
            + createUpperLowerCaseWord("Variables") + "[\\s]*:" + "|"
            + createUpperLowerCaseWord("Variables") + ")");


    public VariableDeclarationRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_VARIABLES_DECLARATION);
    }
}
