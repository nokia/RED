/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.List;

import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;

import com.google.common.collect.Range;

class TemplateKeywordCallArgumentsValidator extends KeywordCallArgumentsValidator {

    private final String keywordName;

    private final int lineNumber;

    private final int startOffset;

    private final int endOffset;

    TemplateKeywordCallArgumentsValidator(final FileValidationContext validationContext,
            final ValidationReportingStrategy reporter, final String keywordName,
            final ArgumentsDescriptor argumentsDescriptor, final List<RobotToken> arguments) {
        super(validationContext, null, reporter, argumentsDescriptor, arguments);
        this.keywordName = keywordName;
        this.lineNumber = arguments.stream().findFirst().map(RobotToken::getLineNumber).orElse(-1);
        this.startOffset = arguments.stream().findFirst().map(RobotToken::getStartOffset).orElse(-1);
        this.endOffset = arguments.stream().reduce((a, b) -> b).map(RobotToken::getEndOffset).orElse(-1);
    }


    @Override
    protected String getKeywordName() {
        return keywordName;
    }

    @Override
    protected ProblemPosition getKeywordProblemPosition() {
        return new ProblemPosition(lineNumber, Range.closed(startOffset, endOffset));
    }
}
