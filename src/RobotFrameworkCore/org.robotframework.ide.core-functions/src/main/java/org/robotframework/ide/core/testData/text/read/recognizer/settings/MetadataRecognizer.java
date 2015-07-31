package org.robotframework.ide.core.testData.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;


public class MetadataRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?("
            + createUpperLowerCaseWord("Metadata") + "[\\s]*:" + "|"
            + createUpperLowerCaseWord("Metadata") + ")");


    public MetadataRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_METADATA_DECLARATION);
    }
}
