package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.List;


public class AExecutableElementSettingsRecognizer extends ATokenRecognizer {

    protected AExecutableElementSettingsRecognizer(
            final List<String> settingName, RobotTokenType type) {
        // Pattern.compile("[ ]?\\[\\s*"
        // + createUpperLowerCaseWord(settingName) + "\\s*\\]")
        super(null, type);
    }
}
