package org.robotframework.ide.core.testData.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;


public class ResourceDeclarationRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?"
            + createUpperLowerCaseWord("Resource"));


    public ResourceDeclarationRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_RESOURCE_DECLARATION);
    }
}
