/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.text.read.recognizer.header.TasksTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.TestCasesTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder;

public class RobotSuiteFileDescriber extends ASuiteFileDescriber {

    private static final Pattern REQUIRED_PATTERN = Pattern
            .compile("^" + TestCasesTableHeaderRecognizer.EXPECTED.pattern() + "(\\s)?$");
    private static final Pattern FORBIDDEN_PATTERN = Pattern
            .compile("^" + TasksTableHeaderRecognizer.EXPECTED.pattern() + "(\\s)?$");

    public RobotSuiteFileDescriber() {
        super(new TokenSeparatorBuilder(FileFormat.TXT_OR_ROBOT), REQUIRED_PATTERN, FORBIDDEN_PATTERN);
    }

    @Override
    protected String getContentTypeId() {
        return ASuiteFileDescriber.SUITE_FILE_ROBOT_CONTENT_ID;
    }
}
