/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.ArrayList;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;

class KeywordCallInTemplateValidator extends KeywordCallValidator {

    KeywordCallInTemplateValidator(final FileValidationContext validationContext, final RobotToken keywordNameToken,
            final ValidationReportingStrategy reporter) {
        super(validationContext, keywordNameToken, new ArrayList<>(), reporter);
    }

    @Override
    protected void validateArguments() {
        // templates have no arguments which needs validation; only keyword requires validating
    }
}
