/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.KeywordReturn;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.keywords.DeprecatedKeywordHeaderAlias;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.keywords.DocumentationUserKeywordDeclarationSettingValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.keywords.PostconditionDeclarationExistenceValidator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

class KeywordTableValidator implements ModelUnitValidator {

    private final Optional<RobotKeywordsSection> keywordSection;

    private final ValidationReportingStrategy reporter;

    private final FileValidationContext validationContext;

    private static final Pattern VARIABLES_ONLY_PATTERN = Pattern.compile("^([$&@]\\{[^{}]+\\})+$");

    KeywordTableValidator(final FileValidationContext validationContext,
            final Optional<RobotKeywordsSection> keywordSection, final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.keywordSection = keywordSection;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!keywordSection.isPresent()) {
            return;
        }

        final RobotKeywordsSection robotKeywordsSection = keywordSection.get();
        final KeywordTable keywordTable = robotKeywordsSection.getLinkedElement();
        final List<UserKeyword> keywords = keywordTable.getKeywords();

        validateByExternal(robotKeywordsSection, monitor);

        reportEmptyKeywords(keywords);
        reportWrongKeywordName(keywords);
        reportMaskingKeywords(keywords);
        reportDuplicatedKeywords(keywords);
        reportSettingsProblems(keywords);
        reportKeywordUsageProblems(keywords);
        reportUnknownVariables(keywords);
        reportVariableAsKeywordName(keywords);
    }

    private void validateByExternal(final RobotKeywordsSection section, final IProgressMonitor monitor)
            throws CoreException {
        new DocumentationUserKeywordDeclarationSettingValidator(validationContext.getFile(), section, reporter)
                .validate(monitor);
        new PostconditionDeclarationExistenceValidator(validationContext.getFile(), reporter, section)
                .validate(monitor);
        new DeprecatedKeywordHeaderAlias(validationContext.getFile(), reporter, section).validate(monitor);
    }

    private void reportEmptyKeywords(final List<UserKeyword> keywords) {
        for (final UserKeyword keyword : keywords) {
            final RobotToken keywordName = keyword.getKeywordName();
            final String name = keywordName.getText();
            if (isReturnEmpty(keyword) && !hasAnythingToExecute(keyword)) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.EMPTY_KEYWORD)
                        .formatMessageWith(name);
                final Map<String, Object> arguments = ImmutableMap.of(AdditionalMarkerAttributes.NAME, name);
                reporter.handleProblem(problem, validationContext.getFile(), keywordName, arguments);
            }
        }
    }

    private void reportWrongKeywordName(final List<UserKeyword> keywords) {
        for (final UserKeyword keyword : keywords) {
            final RobotToken keywordName = keyword.getKeywordName();
            final String name = keywordName.getText();
            if (name.contains(".")) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.KEYWORD_NAME_WITH_DOTS)
                        .formatMessageWith(name);
                final Map<String, Object> arguments = ImmutableMap.of(AdditionalMarkerAttributes.NAME, name);
                reporter.handleProblem(problem, validationContext.getFile(), keywordName, arguments);
            }
        }
    }

    private void reportMaskingKeywords(final List<UserKeyword> keywords) {
        for (final UserKeyword keyword : keywords) {
            final RobotToken keywordName = keyword.getKeywordName();
            final String name = keywordName.getText();
            final ListMultimap<KeywordScope, KeywordEntity> possibleKeywords = validationContext
                    .getPossibleKeywords(name, true);
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

    }

    private boolean hasAnythingToExecute(final UserKeyword keyword) {
        for (final RobotExecutableRow<?> robotExecutableRow : keyword.getExecutionContext()) {
            if (robotExecutableRow.isExecutable()) {
                return true;
            }
        }
        return false;
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

    private void reportDuplicatedKeywords(final List<UserKeyword> keywords) {
        final Set<String> duplicatedNames = newHashSet();

        for (final UserKeyword kw1 : keywords) {
            for (final UserKeyword kw2 : keywords) {
                if (kw1 != kw2) {
                    final String kw1Name = QualifiedKeywordName.unifyDefinition(kw1.getKeywordName().getText());
                    final String kw2Name = QualifiedKeywordName.unifyDefinition(kw2.getKeywordName().getText());

                    if (kw1Name.equals(kw2Name)) {
                        duplicatedNames.add(kw1Name);
                    }
                }
            }
        }

        for (final UserKeyword keyword : keywords) {
            final RobotToken keywordName = keyword.getKeywordName();
            final String name = keywordName.getText();

            if (duplicatedNames.contains(QualifiedKeywordName.unifyDefinition(name.toLowerCase()))) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.DUPLICATED_KEYWORD)
                        .formatMessageWith(name);
                final Map<String, Object> additionalArguments = ImmutableMap.of(AdditionalMarkerAttributes.NAME, name);
                reporter.handleProblem(problem, validationContext.getFile(), keywordName, additionalArguments);
            }
        }
    }

    private void reportSettingsProblems(final List<UserKeyword> keywords) throws CoreException {
        for (final UserKeyword keyword : keywords) {
            new KeywordSettingsValidator(validationContext, keyword, reporter).validate(null);
        }
    }

    private void reportKeywordUsageProblems(final List<UserKeyword> keywords) {
        TestCaseTableValidator.reportKeywordUsageProblems(validationContext, reporter, findExecutableRows(keywords),
                Optional.empty());
    }

    private List<RobotExecutableRow<?>> findExecutableRows(final List<UserKeyword> keywords) {
        final List<RobotExecutableRow<?>> executables = newArrayList();
        for (final UserKeyword keyword : keywords) {
            executables.addAll(keyword.getExecutionContext());
        }
        return executables;
    }

    private void reportUnknownVariables(final List<UserKeyword> keywords) {
        final Set<String> variables = validationContext.getAccessibleVariables();
        final VariableExtractor variableExtractor = new VariableExtractor();
        final String fileName = validationContext.getFile().getName();

        for (final UserKeyword keyword : keywords) {
            final Set<String> allVariables = newHashSet(variables);
            allVariables.addAll(extractArgumentVariables(keyword, variableExtractor, fileName));

            reportUnknownVariablesInTimeoutSetting(keyword, allVariables);
            reportUnknownVariablesInTags(keyword, allVariables);
            TestCaseTableValidator.reportUnknownVariables(validationContext, reporter,
                    collectKeywordExeRowsForVariablesChecking(keyword), allVariables);
        }
    }

    private List<? extends RobotExecutableRow<?>> collectKeywordExeRowsForVariablesChecking(final UserKeyword keyword) {
        final List<RobotExecutableRow<?>> exeRows = newArrayList();
        exeRows.addAll(keyword.getExecutionContext());

        final List<KeywordTeardown> teardowns = keyword.getTeardowns();
        if (!teardowns.isEmpty()) {
            exeRows.add(teardowns.get(0).asExecutableRow());
        }
        final List<KeywordReturn> returns = keyword.getReturns();
        if (!returns.isEmpty()) {
            exeRows.add(returns.get(0).asExecutableRow());
        }

        return exeRows;
    }

    private Collection<String> extractArgumentVariables(final UserKeyword keyword, final VariableExtractor extractor,
            final String fileName) {
        final Set<String> arguments = newHashSet();

        // first add arguments embedded in name, then from [Arguments] setting
        arguments.addAll(newArrayList(transform(
                VariableNamesSupport
                        .extractUnifiedVariables(newArrayList(keyword.getKeywordName()), extractor, fileName).keySet(),
                EmbeddedKeywordNamesSupport::removeRegex)));
        for (final KeywordArguments argument : keyword.getArguments()) {
            arguments.addAll(VariableNamesSupport.extractUnifiedVariables(argument.getArguments(), extractor, fileName)
                    .keySet());
        }
        return arguments;
    }

    private void reportUnknownVariablesInTags(final UserKeyword keyword, final Set<String> variables) {
        final List<KeywordTags> tags = keyword.getTags();

        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reporter);
        for (final KeywordTags tag : tags) {
            unknownVarsValidator.reportUnknownVars(tag.getTags(), variables);
        }
    }

    private void reportUnknownVariablesInTimeoutSetting(final UserKeyword keyword, final Set<String> variables) {
        final List<KeywordTimeout> timeouts = keyword.getTimeouts();
        for (final KeywordTimeout keywordTimeout : timeouts) {
            final RobotToken timeoutToken = keywordTimeout.getTimeout();
            if (timeoutToken != null) {
                TestCaseTableValidator.validateTimeoutSetting(validationContext, reporter, variables, timeoutToken);
            }
        }
    }

    private void reportVariableAsKeywordName(final List<UserKeyword> keywords) {

        for (final UserKeyword keyword : keywords) {
            final RobotToken keywordName = keyword.getKeywordName();
            final String name = keywordName.getText();

            final Matcher variablesOnlyMatcher = VARIABLES_ONLY_PATTERN.matcher(name);

            if (variablesOnlyMatcher.matches()) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.VARIABLE_AS_KEYWORD_NAME)
                        .formatMessageWith(name);

                final Map<String, Object> arguments = ImmutableMap.of(AdditionalMarkerAttributes.NAME, name);
                reporter.handleProblem(problem, validationContext.getFile(), keywordName, arguments);
            }
        }
    }
}
