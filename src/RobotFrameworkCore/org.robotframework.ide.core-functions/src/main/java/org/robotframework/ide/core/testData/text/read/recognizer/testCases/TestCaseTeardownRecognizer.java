/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer.testCases;

import org.robotframework.ide.core.testData.text.read.recognizer.AExecutableElementSettingsRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestCaseTeardownRecognizer extends
        AExecutableElementSettingsRecognizer {

    public TestCaseTeardownRecognizer() {
        super(RobotTokenType.TEST_CASE_SETTING_TEARDOWN);
    }
}
