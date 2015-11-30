/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class MetadataRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?(" + createUpperLowerCaseWord("Metadata") + "[\\s]*:"
            + "|" + createUpperLowerCaseWord("Metadata") + ")|(" + createUpperLowerCaseWord("Meta") + "[\\s]*:" + "|"
            + createUpperLowerCaseWord("Meta") + ")");

    public MetadataRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_METADATA_DECLARATION);
    }

    @Override
    public ATokenRecognizer newInstance() {
        return new MetadataRecognizer();
    }
}
