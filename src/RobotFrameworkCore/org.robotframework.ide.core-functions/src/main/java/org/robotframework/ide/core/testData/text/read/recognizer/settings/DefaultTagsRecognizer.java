package org.robotframework.ide.core.testData.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class DefaultTagsRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?("
            + createUpperLowerCaseWord("Default") + "[\\s]+"
            + createUpperLowerCaseWord("Tags") + "[\\s]*:" + "|"
            + createUpperLowerCaseWord("Default") + "[\\s]+"
            + createUpperLowerCaseWord("Tags") + ")");


    public DefaultTagsRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_DEFAULT_TAGS_DECLARATION);
    }
}
