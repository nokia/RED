/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer.userKeywords;

import org.robotframework.ide.core.testData.text.read.recognizer.AExecutableElementSettingsRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class KeywordTimeoutRecognizer extends
        AExecutableElementSettingsRecognizer {

    public KeywordTimeoutRecognizer() {
        super(RobotTokenType.KEYWORD_SETTING_TIMEOUT);
    }
}
