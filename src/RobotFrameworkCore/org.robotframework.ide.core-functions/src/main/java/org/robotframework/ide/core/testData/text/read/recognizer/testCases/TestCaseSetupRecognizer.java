/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer.testCases;

import org.robotframework.ide.core.testData.text.read.recognizer.AExecutableElementSettingsRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestCaseSetupRecognizer extends
        AExecutableElementSettingsRecognizer {

    public TestCaseSetupRecognizer() {
        super(RobotTokenType.TEST_CASE_SETTING_SETUP);
    }
}
