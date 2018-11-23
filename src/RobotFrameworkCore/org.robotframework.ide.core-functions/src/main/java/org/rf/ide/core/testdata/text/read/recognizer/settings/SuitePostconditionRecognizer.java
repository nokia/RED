/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class SuitePostconditionRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?"
            + createUpperLowerCaseWord("Suite") + "[\\s]+"
            + createUpperLowerCaseWord("Postcondition") + "([\\s]*:)?");


    public SuitePostconditionRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION);
    }

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isOlderThan(new RobotVersion(3, 1));
    }

    @Override
    public ATokenRecognizer newInstance() {
        return new SuitePostconditionRecognizer();
    }
}
