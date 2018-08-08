/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.keywords;

import java.util.regex.Pattern;

import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class KeywordDocumentationRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern
            .compile("[ ]?((\\[\\s*" + createUpperLowerCaseWord("Documentation") + "\\s*\\]))");

    public KeywordDocumentationRecognizer() {
        super(EXPECTED, RobotTokenType.KEYWORD_SETTING_DOCUMENTATION);
    }

    @Override
    public ATokenRecognizer newInstance() {
        return new KeywordDocumentationRecognizer();
    }
}
