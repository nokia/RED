/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.keywords;

import java.util.regex.Pattern;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class KeywordPostconditionRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern
            .compile("[ ]?((\\[\\s*" + createUpperLowerCaseWord("Postcondition") + "\\s*\\]))");

    public KeywordPostconditionRecognizer() {
        super(EXPECTED, RobotTokenType.KEYWORD_SETTING_TEARDOWN);
    }

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        // [Postcondition] is not recognized in e.g. 2.9, but it is working in 3.0, however warning
        // is shown that it is deprecated and finally this setting is removed from 3.1
        return robotVersion.isNewerOrEqualTo(new RobotVersion(3, 0))
                && robotVersion.isOlderThan(new RobotVersion(3, 1));
    }

    @Override
    public ATokenRecognizer newInstance() {
        return new KeywordPostconditionRecognizer();
    }
}
