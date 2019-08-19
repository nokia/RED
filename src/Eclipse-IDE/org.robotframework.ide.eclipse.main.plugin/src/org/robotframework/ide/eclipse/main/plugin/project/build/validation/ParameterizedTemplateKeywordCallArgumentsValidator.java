/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.ValidationKeywordEntity;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

class ParameterizedTemplateKeywordCallArgumentsValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final ValidationReportingStrategy reporter;

    private final String keywordName;

    private final ValidationKeywordEntity foundKeyword;

    private final RangeSet<Integer> templateParameters;

    private final int lineNumber;

    private final int startOffset;

    private final int endOffset;

    private final List<RobotToken> arguments;

    ParameterizedTemplateKeywordCallArgumentsValidator(final FileValidationContext validationContext,
            final ValidationReportingStrategy reporter, final String keywordName,
            final ValidationKeywordEntity foundKeyword, final RangeSet<Integer> templateParameters,
            final List<RobotToken> arguments) {
        this.validationContext = validationContext;
        this.reporter = reporter;
        this.keywordName = keywordName;
        this.foundKeyword = foundKeyword;
        this.templateParameters = templateParameters;
        this.lineNumber = arguments.stream().findFirst().map(RobotToken::getLineNumber).orElse(-1);
        this.startOffset = arguments.stream().findFirst().map(RobotToken::getStartOffset).orElse(-1);
        this.endOffset = arguments.stream().reduce((a, b) -> b).map(RobotToken::getEndOffset).orElse(-1);
        this.arguments = arguments;
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        final int expectedArguments = templateParameters.asRanges().size();
        final int actualArguments = arguments.size();
        if (expectedArguments != actualArguments) {
            reporter.handleProblem(
                    RobotProblem.causedBy(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERIZED_TEMPLATE_PARAMETERS)
                            .formatMessageWith(keywordName, expectedArguments, actualArguments),
                    validationContext.getFile(), getArgumentsProblemPosition());
        } else if (foundKeyword == null) {
            final String keywordNameWithResolvedParameters = resolveParameters();
            if (validationContext.findAccessibleKeyword(keywordNameWithResolvedParameters) == null) {
                reporter.handleProblem(
                        RobotProblem.causedBy(KeywordsProblem.UNKNOWN_TEMPLATE_KEYWORD)
                                .formatMessageWith(keywordNameWithResolvedParameters),
                        validationContext.getFile(), getArgumentsProblemPosition());
            }
        }
    }

    private String resolveParameters() {
        final StringBuilder builder = new StringBuilder(keywordName);
        final Iterator<Range<Integer>> rangesIterator = templateParameters.asRanges().iterator();
        final Iterator<RobotToken> argsIterator = arguments.iterator();
        int offset = 0;
        while (rangesIterator.hasNext() && argsIterator.hasNext()) {
            final Range<Integer> range = rangesIterator.next();
            final String text = argsIterator.next().getText();
            builder.replace(range.lowerEndpoint() + offset, range.upperEndpoint() + offset + 1, text);
            offset += text.length() + range.lowerEndpoint() - range.upperEndpoint() - 1;
        }
        return builder.toString();
    }

    private ProblemPosition getArgumentsProblemPosition() {
        return new ProblemPosition(lineNumber, Range.closed(startOffset, endOffset));
    }
}
