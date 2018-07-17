/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.keywords.KeywordReturn;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;


class KeywordValidator implements ModelUnitValidator {

    private static final Pattern VARIABLES_ONLY_PATTERN = Pattern.compile("^([$&@]\\{[^{}]+\\})+$");

    private final FileValidationContext validationContext;
    private final ValidationReportingStrategy reporter;

    private final UserKeyword keyword;

    KeywordValidator(final FileValidationContext validationContext, final UserKeyword keyword,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.keyword = keyword;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        reportVariableAsKeywordName();
        reportKeywordNameWithDots();
        reportMaskingKeyword();
        reportEmptyKeyword();

        validateSettings();
        validateKeywordsAndVariablesUsages();
    }

    private void reportVariableAsKeywordName() {
        final RobotToken keywordName = keyword.getKeywordName();
        final String name = keywordName.getText();

        if (VARIABLES_ONLY_PATTERN.matcher(name).matches()) {
            final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.VARIABLE_AS_KEYWORD_NAME)
                    .formatMessageWith(name);

            final Map<String, Object> arguments = ImmutableMap.of(AdditionalMarkerAttributes.NAME, name);
            reporter.handleProblem(problem, validationContext.getFile(), keywordName, arguments);
        }
    }

    private void reportKeywordNameWithDots() {
        final RobotToken keywordName = keyword.getKeywordName();
        final String name = keywordName.getText();
        if (name.contains(".")) {
            final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.KEYWORD_NAME_WITH_DOTS)
                    .formatMessageWith(name);
            final Map<String, Object> arguments = ImmutableMap.of(AdditionalMarkerAttributes.NAME, name);
            reporter.handleProblem(problem, validationContext.getFile(), keywordName, arguments);
        }
    }

    private void reportMaskingKeyword() {
        final RobotToken keywordName = keyword.getKeywordName();
        final String name = keywordName.getText();
        final ListMultimap<KeywordScope, KeywordEntity> possibleKeywords = validationContext.getPossibleKeywords(name,
                true);
        for (final KeywordScope scope : KeywordScope.defaultOrder()) {
            if (scope != KeywordScope.LOCAL && !possibleKeywords.get(scope).isEmpty()) {
                final KeywordEntity maskedKeyword = possibleKeywords.get(scope).get(0);
                final String maskedName = maskedKeyword.getNameFromDefinition();
                final String maskedSource = maskedKeyword.getSourceNameInUse();
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.KEYWORD_MASKS_OTHER_KEYWORD)
                        .formatMessageWith(name, maskedName, maskedSource, maskedSource + "." + maskedName);
                reporter.handleProblem(problem, validationContext.getFile(), keywordName);
            }
        }
    }

    private void reportEmptyKeyword() {
        final RobotToken keywordName = keyword.getKeywordName();
        final String name = keywordName.getText();
        if (isReturnEmpty(keyword) && !hasAnythingToExecute(keyword)) {
            final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.EMPTY_KEYWORD).formatMessageWith(name);
            final Map<String, Object> arguments = ImmutableMap.of(AdditionalMarkerAttributes.NAME, name);
            reporter.handleProblem(problem, validationContext.getFile(), keywordName, arguments);
        }
    }

    private boolean hasAnythingToExecute(final UserKeyword keyword) {
        return keyword.getExecutionContext().stream().anyMatch(RobotExecutableRow::isExecutable);
    }

    private boolean isReturnEmpty(final UserKeyword keyword) {
        if (!keyword.getReturns().isEmpty()) {
            final KeywordReturn keywordReturn = keyword.getReturns().get(keyword.getReturns().size() - 1);
            final List<RobotToken> returnValues = keywordReturn.getReturnValues();
            for (final RobotToken rtValue : returnValues) {
                if (!rtValue.getText().trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void validateSettings() {
        new KeywordSettingsValidator(validationContext, keyword, reporter).validate();
    }

    private void validateKeywordsAndVariablesUsages() {
        final Set<String> additionalVariables = new HashSet<>();
        additionalVariables.addAll(extractArgumentVariables());

        final List<ExecutableValidator> execValidators = new ArrayList<>();
        // not validated; will just add variables if any
        getGeneralSettingsSuiteSetups().stream()
                .findFirst()
                .map(suiteSetup -> ExecutableValidator.of(validationContext, additionalVariables, suiteSetup,
                        new SilentReporter()))
                .ifPresent(execValidators::add);

        keyword.getExecutionContext().stream()
                .filter(RobotExecutableRow::isExecutable)
                .map(row -> ExecutableValidator.of(validationContext, additionalVariables, row, reporter))
                .forEach(execValidators::add);
        keyword.getTeardowns().stream()
                .findFirst()
                .map(teardown -> ExecutableValidator.of(validationContext, additionalVariables, teardown, reporter))
                .ifPresent(execValidators::add);
        execValidators.forEach(ExecutableValidator::validate);

        // also validate variables in [Return] after all executables were checked (that's why this
        // is done here not in KeywordSettingsValidator)
        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reporter);
        for (final KeywordReturn kwReturn : keyword.getReturns()) {
            unknownVarsValidator.reportUnknownVars(additionalVariables, kwReturn.getReturnValues());
        }
    }

    private Collection<String> extractArgumentVariables() {
        final String fileName = validationContext.getFile().getName();
        final VariableExtractor extractor = new VariableExtractor();

        final Set<String> arguments = new HashSet<>();
        // first add arguments embedded in name
        Stream.of(keyword.getKeywordName())
                .map(nameToken -> VariableNamesSupport.extractUnifiedVariables(nameToken, extractor, fileName))
                .map(Multimap::keySet)
                .flatMap(Set::stream)
                .map(EmbeddedKeywordNamesSupport::removeRegex)
                .forEach(arguments::add);

        // second add arguments from [Arguments] setting
        keyword.getArguments()
                .stream()
                .map(arg -> arg.getArguments())
                .flatMap(List::stream)
                .map(argToken -> VariableNamesSupport.extractUnifiedVariables(argToken, extractor, fileName))
                .map(Multimap::keySet)
                .flatMap(Set::stream)
                .forEach(arguments::add);
        return arguments;
    }

    private List<SuiteSetup> getGeneralSettingsSuiteSetups() {
        final RobotFile fileModel = keyword.getParent().getParent();
        return fileModel.getSettingTable().getSuiteSetups();
    }
}
