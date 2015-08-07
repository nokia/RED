package org.robotframework.ide.core.testData.text.read.recognizer.userKeywords;

import org.robotframework.ide.core.testData.text.read.recognizer.AExecutableElementSettingsRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class KeywordTimeoutRecognizer extends
        AExecutableElementSettingsRecognizer {

    public KeywordTimeoutRecognizer() {
        super(RobotTokenType.KEYWORD_SETTING_TIMEOUT);
    }
}
