/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.TemplateSetting;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.KeywordCallValidator;

import com.google.common.collect.Range;


public class TemplateSettingUntilRf31Validator extends VersionDependentModelUnitValidator {

    private final FileValidationContext validationContext;

    private final ValidationReportingStrategy reporter;

    private final List<? extends TemplateSetting> templates;

    TemplateSettingUntilRf31Validator(final FileValidationContext validationContext,
            final List<? extends TemplateSetting> templates, final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.templates = templates;
        this.reporter = reporter;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.lessThan(new RobotVersion(3, 2));
    }


    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        reportTemplateWrittenInMultipleCells();
        reportTemplateKeywordProblems();
    }

    private void reportTemplateWrittenInMultipleCells() {
        for (final TemplateSetting template : templates) {
            if (!template.getUnexpectedArguments().isEmpty()) {
                final RobotToken settingToken = template.getDeclaration();
                final RobotProblem problem = RobotProblem
                        .causedBy(GeneralSettingsProblem.TEMPLATE_KEYWORD_NAME_IN_MULTIPLE_CELLS);
                reporter.handleProblem(problem, validationContext.getFile(), settingToken);
            }
        }
    }

    private void reportTemplateKeywordProblems() {
        for (final TemplateSetting template : templates) {
            final RobotToken keywordToken = template.getKeywordName();
            if (keywordToken != null) {
                final List<String> keywordParts = newArrayList(keywordToken.getText());
                template.getUnexpectedArguments().stream().map(RobotToken::getText).forEach(keywordParts::add);

                final String keywordName = String.join(" ", keywordParts);
                if (keywordName.toLowerCase().equals("none")) {
                    continue;
                }
                new KeywordCallInTemplateValidator(validationContext, keywordName, keywordToken, reporter).validate();
            }
        }
    }

    private class KeywordCallInTemplateValidator extends KeywordCallValidator {

        private final String keywordName;

        KeywordCallInTemplateValidator(final FileValidationContext validationContext, final String keywordName,
                final RobotToken keywordNameToken, final ValidationReportingStrategy reporter) {
            super(validationContext, keywordNameToken, new ArrayList<>(), reporter);
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
