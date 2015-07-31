package org.robotframework.ide.core.testData.text.read.recognizer.header;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class SettingsTableHeaderRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?[*]+[\\s]*("
            + createUpperLowerCaseWord("Settings") + "|"
            + createUpperLowerCaseWord("Setting") + "|"
            + createUpperLowerCaseWord("Metadata") + ")[\\s]*[*]*");


    public SettingsTableHeaderRecognizer() {
        super(EXPECTED, RobotTokenType.SETTINGS_TABLE_HEADER);
    }
}
