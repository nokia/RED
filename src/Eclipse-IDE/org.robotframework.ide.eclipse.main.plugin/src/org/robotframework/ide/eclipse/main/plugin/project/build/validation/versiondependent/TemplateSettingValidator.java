/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.TemplateSetting;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.KeywordCallValidator;

import com.google.common.collect.Range;


class TemplateSettingValidator extends VersionDependentModelUnitValidator {

    private final FileValidationContext validationContext;

    private final ValidationReportingStrategy reporter;

    private final List<? extends TemplateSetting> templates;

    TemplateSettingValidator(final FileValidationContext validationContext,
            final List<? extends TemplateSetting> templates, final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.templates = templates;
        this.reporter = reporter;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.atLeast(new RobotVersion(3, 2));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        for (final TemplateSetting template : templates) {
            final RobotToken keywordToken = template.getKeywordName();
            if (keywordToken != null && !keywordToken.getText().equalsIgnoreCase("none")
                    && template.getUnexpectedArguments().isEmpty()) {
                new KeywordCallInTemplateValidator(validationContext, keywordToken.getText(), keywordToken, reporter)
                        .validate();
            }
        }
    }

    static class KeywordCallInTemplateValidator extends KeywordCallValidator {

        private final String keywordName;

        KeywordCallInTemplateValidator(final FileValidationContext validationContext, final String keywordName,
                final RobotToken keywordNameToken, final ValidationReportingStrategy reporter) {
            super(validationContext, keywordNameToken, null, reporter);
            this.keywordName = keywordName;
        }

        @Override
        protected String getActualKeywordName() {
            return keywordName;
        }

        @Override
        protected void validateKeywordCall() {
            // only keyword calls without embedded arguments can be validated in template setting
            if (!EmbeddedKeywordNamesSupport.hasEmbeddedArguments(keywordName)) {
                super.validateKeywordCall();
            }
        }

        @Override
        protected void validateArguments() {
            // templates have no arguments which needs validation; only keyword requires validating
        }
    }
}
