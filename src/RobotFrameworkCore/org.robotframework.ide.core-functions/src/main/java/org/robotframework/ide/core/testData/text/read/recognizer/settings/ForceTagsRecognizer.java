package org.robotframework.ide.core.testData.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class ForceTagsRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?("
            + createUpperLowerCaseWord("Force") + "[\\s]+"
            + createUpperLowerCaseWord("Tags") + "[\\s]*:" + "|"
            + createUpperLowerCaseWord("Force") + "[\\s]+"
            + createUpperLowerCaseWord("Tags") + ")");


    public ForceTagsRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_FORCE_TAGS_DECLARATION);
    }
}
