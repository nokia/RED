package org.robotframework.ide.core.testData.text.read.recognizer.userKeywords;

import org.robotframework.ide.core.testData.text.read.recognizer.AExecutableElementSettingsRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class KeywordArgumentsRecognizer extends
        AExecutableElementSettingsRecognizer {

    public KeywordArgumentsRecognizer() {
        super(RobotTokenType.KEYWORD_SETTING_ARGUMENTS);
    }
}
