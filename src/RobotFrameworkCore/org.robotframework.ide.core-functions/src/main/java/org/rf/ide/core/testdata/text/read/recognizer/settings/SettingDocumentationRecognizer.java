/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class SettingDocumentationRecognizer extends ATokenRecognizer {

    public static final RobotTokenType TOKEN_TYPE = RobotTokenType.SETTING_DOCUMENTATION_DECLARATION;

    public static final Pattern EXPECTED = Pattern.compile("[ ]?(" + createUpperLowerCaseWord("Documentation")
            + "[\\s]*:" + "|" + createUpperLowerCaseWord("Documentation") + ")|("
            + createUpperLowerCaseWord("Document") + "[\\s]*:" + "|" + createUpperLowerCaseWord("Document") + ")");

    public SettingDocumentationRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_DOCUMENTATION_DECLARATION);
    }

    @Override
    public ATokenRecognizer newInstance() {
        return new SettingDocumentationRecognizer();
    }
}
