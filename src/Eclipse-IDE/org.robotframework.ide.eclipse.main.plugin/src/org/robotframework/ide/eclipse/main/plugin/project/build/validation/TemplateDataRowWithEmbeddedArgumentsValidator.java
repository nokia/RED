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

import com.google.common.collect.RangeSet;

class TemplateDataRowWithEmbeddedArgumentsValidator implements ExecutableValidator {

    private final FileValidationContext validationContext;

    private final Set<String> additionalVariables;

    private final String keywordName;

    private final ValidationKeywordEntity foundKeyword;

    private final RangeSet<Integer> embeddedArguments;

    private final List<RobotToken> arguments;

    private final ValidationReportingStrategy reporter;

    public TemplateDataRowWithEmbeddedArgumentsValidator(final FileValidationContext validationContext,
            final Set<String> additionalVariables, final String keywordName, final ValidationKeywordEntity foundKeyword,
            final RangeSet<Integer> embeddedArguments, final List<RobotToken> arguments,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.additionalVariables = additionalVariables;
        this.keywordName = keywordName;
        this.foundKeyword = foundKeyword;
        this.embeddedArguments = embeddedArguments;
        this.arguments = arguments;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        final TemplateKeywordCallEmbeddedArgumentsValidator argsValidator = new TemplateKeywordCallEmbeddedArgumentsValidator(
                validationContext, reporter, keywordName, foundKeyword, embeddedArguments, arguments);
        argsValidator.validate(monitor);

        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext,
                AttributesAugmentingReportingStrategy.create(reporter, Collections.emptyMap()));

        unknownVarsValidator.reportUnknownVars(additionalVariables, arguments);
    }
}
