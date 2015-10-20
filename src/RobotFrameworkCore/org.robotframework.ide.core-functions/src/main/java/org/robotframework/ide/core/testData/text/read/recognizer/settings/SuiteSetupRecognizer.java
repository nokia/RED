/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class SuiteSetupRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?(("
            + createUpperLowerCaseWord("Suite") + "[\\s]+"
            + createUpperLowerCaseWord("Setup") + "[\\s]*:" + "|"
            + createUpperLowerCaseWord("Suite") + "[\\s]+"
            + createUpperLowerCaseWord("Setup") + ")|("
            + createUpperLowerCaseWord("Suite") + "[\\s]+"
            + createUpperLowerCaseWord("Precondition") + "[\\s]*:" + "|"
            + createUpperLowerCaseWord("Suite") + "[\\s]+"
            + createUpperLowerCaseWord("Precondition") + "))");


    public SuiteSetupRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_SUITE_SETUP_DECLARATION);
    }


    @Override
    public ATokenRecognizer newInstance() {
        return new SuiteSetupRecognizer();
    }
}
