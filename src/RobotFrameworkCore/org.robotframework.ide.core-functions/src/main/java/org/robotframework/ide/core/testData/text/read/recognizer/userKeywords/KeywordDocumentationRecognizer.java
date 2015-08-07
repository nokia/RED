package org.robotframework.ide.core.testData.text.read.recognizer.userKeywords;

import org.robotframework.ide.core.testData.text.read.recognizer.AExecutableElementSettingsRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class KeywordDocumentationRecognizer extends
        AExecutableElementSettingsRecognizer {

    public KeywordDocumentationRecognizer() {
        super(RobotTokenType.KEYWORD_SETTING_DOCUMENTATION);
    }
}
