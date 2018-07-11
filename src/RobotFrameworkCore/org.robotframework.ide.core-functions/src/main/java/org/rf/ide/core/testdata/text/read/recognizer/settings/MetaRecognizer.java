/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class MetaRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?(" + createUpperLowerCaseWord("Meta") + ":)");

    public MetaRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_METADATA_DECLARATION);
    }

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion != null && robotVersion.isOlderThan(new RobotVersion(3, 0));
    }

    @Override
    public ATokenRecognizer newInstance() {
        return new MetaRecognizer();
    }
}
