/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer.header;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class SettingsTableHeaderRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern
            .compile("[ ]?([*][\\s]*)+[\\s]*("
                    + createUpperLowerCaseWord("Settings") + "|"
                    + createUpperLowerCaseWord("Setting") + "|"
                    + createUpperLowerCaseWord("Metadata") + ")([\\s]*[*])*");


    public SettingsTableHeaderRecognizer() {
        super(EXPECTED, RobotTokenType.SETTINGS_TABLE_HEADER);
    }
}
