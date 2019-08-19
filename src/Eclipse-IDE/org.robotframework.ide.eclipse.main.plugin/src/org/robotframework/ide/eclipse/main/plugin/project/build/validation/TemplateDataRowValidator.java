/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.AttributesAugmentingReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.ValidationKeywordEntity;

class TemplateDataRowValidator implements ExecutableValidator {

    private final FileValidationContext validationContext;

    private final Set<String> additionalVariables;

    private final ValidationKeywordEntity foundKeyword;

    private final List<RobotToken> arguments;

    private final ValidationReportingStrategy reporter;

    public TemplateDataRowValidator(final FileValidationContext validationContext,
            final Set<String> additionalVariables, final ValidationKeywordEntity foundKeyword,
            final List<RobotToken> arguments, final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.additionalVariables = additionalVariables;
        this.foundKeyword = foundKeyword;
        this.arguments = arguments;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        final TemplateKeywordCallArgumentsValidator argsValidator = new TemplateKeywordCallArgumentsValidator(
                validationContext, reporter, foundKeyword.getKeywordName(), foundKeyword.getArgumentsDescriptor(),
                arguments);
        argsValidator.validate(monitor);

        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext,
                AttributesAugmentingReportingStrategy.create(reporter, Collections.emptyMap()));

        unknownVarsValidator.reportUnknownVars(additionalVariables, arguments);
    }
}
