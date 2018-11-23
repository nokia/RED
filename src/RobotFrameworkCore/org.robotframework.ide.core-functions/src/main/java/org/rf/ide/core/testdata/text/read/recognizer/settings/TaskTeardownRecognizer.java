/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskTeardownRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile(
            "[ ]?" + createUpperLowerCaseWord("Task") + "[\\s]+" + createUpperLowerCaseWord("Teardown") + "([\\s]*:)?");

    public TaskTeardownRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_TASK_TEARDOWN_DECLARATION);
    }

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isNewerOrEqualTo(new RobotVersion(3, 1));
    }

    @Override
    public ATokenRecognizer newInstance() {
        return new TaskTeardownRecognizer();
    }
}
