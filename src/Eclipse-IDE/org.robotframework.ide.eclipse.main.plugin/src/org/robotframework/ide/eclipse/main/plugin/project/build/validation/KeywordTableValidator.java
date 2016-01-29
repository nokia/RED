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
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.KeywordReturn;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

class KeywordTableValidator implements ModelUnitValidator {

    private final Optional<RobotKeywordsSection> keywordSection;

    private final ProblemsReportingStrategy reporter;

    private final FileValidationContext validationContext;

    KeywordTableValidator(final FileValidationContext validationContext,
            final Optional<RobotKeywordsSection> keywordSection, final ProblemsReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.keywordSection = keywordSection;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!keywordSection.isPresent()) {
            return;
        }
        final RobotSuiteFile suiteModel = keywordSection.get().getSuiteFile();
        final KeywordTable keywordTable = (KeywordTable) keywordSection.get().getLinkedElement();
        final List<UserKeyword> keywords = keywordTable.getKeywords();

        reportEmptyKeyword(suiteModel.getFile(), keywords);
        reportDuplicatedKewords(suiteModel.getFile(), keywords);
        reportEmbeddedArgumentsConflicts(suiteModel.getFile(), keywords);
        TestCasesTableValidator.reportKeywordUsageProblems(suiteModel, validationContext, reporter,
                findExecutableRows(keywords), Optional.<String> absent());
        reportUnknownVariables(suiteModel.getFile(), keywords);
    }

    private void reportEmptyKeyword(final IFile file, final List<UserKeyword> keywords) {
        for (final UserKeyword keyword : keywords) {
            final RobotToken caseName = keyword.getKeywordName();
            if (keyword.getExecutionContext().isEmpty()) {
                boolean report = true;
                if (!keyword.getReturns().isEmpty()) {
                    final KeywordReturn keywordReturn = keyword.getReturns().get(keyword.getReturns().size() - 1);
                    final List<RobotToken> returnValues = keywordReturn.getReturnValues();
                    report = isReturnEmpty(returnValues);
                }
                if (report) {
                    TestCasesTableValidator.reportEmptyExecutableRows(file, reporter, caseName,
                            keyword.getKeywordExecutionRows(), KeywordsProblem.EMPTY_KEYWORD);
                }
            }
        }
    }

    private boolean isReturnEmpty(final List<RobotToken> returnValues) {
        boolean report = true;
        if (!returnValues.isEmpty()) {
            for (final RobotToken rtValue: returnValues) {
                if (!rtValue.getText().toString().trim().isEmpty()) {
                    report = false;
                    break;
                }
            }
        }
        
        return report;
    }

    private void reportDuplicatedKewords(final IFile file, final List<UserKeyword> keywords) {
        final Set<String> duplicatedNames = newHashSet();

        for (final UserKeyword kw1 : keywords) {
            for (final UserKeyword kw2 : keywords) {
                if (kw1 != kw2) {
                    final String kw1Name = kw1.getKeywordName().getText().toString();
                    final String kw2Name = kw2.getKeywordName().getText().toString();

                    if (kw1Name.equalsIgnoreCase(kw2Name)) {
                        duplicatedNames.add(kw1Name.toLowerCase());
                    }
                }
            }
        }

        for (final UserKeyword keyword : keywords) {
            final RobotToken keywordName = keyword.getKeywordName();
            final String name = keywordName.getText().toString();

            if (duplicatedNames.contains(name.toLowerCase())) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.DUPLICATED_KEYWORD)
                        .formatMessageWith(name);
                final Map<String, Object> additionalArguments = ImmutableMap
                        .<String, Object> of(AdditionalMarkerAttributes.NAME, name);
                reporter.handleProblem(problem, file, keywordName, additionalArguments);
            }
        }
    }

    private void reportEmbeddedArgumentsConflicts(final IFile file, final List<UserKeyword> keywords) {
        final VariableExtractor variableExtractor = new VariableExtractor();
        final String fileName = file.getName();

        for (final UserKeyword keyword : keywords) {
            final List<KeywordArguments> arguments = keyword.getArguments();
            final boolean hasArgumentsSetting = arguments != null && !arguments.isEmpty();

            final List<VariableDeclaration> extractedVariables = variableExtractor
                    .extract(keyword.getKeywordName(), fileName).getCorrectVariables();
            final boolean hasEmbeddedArguments = !extractedVariables.isEmpty();

            if (hasArgumentsSetting && hasEmbeddedArguments) {
                final String name = keyword.getKeywordName().getText();
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.ARGUMENTS_DEFINED_TWICE)
                        .formatMessageWith(name);
                final Map<String, Object> additionalArguments = ImmutableMap.<String, Object> of("name", name);
                reporter.handleProblem(problem, file, keyword.getKeywordName(), additionalArguments);
            }
        }
    }

    private List<RobotExecutableRow<?>> findExecutableRows(final List<UserKeyword> keywords) {
        final List<RobotExecutableRow<?>> executables = newArrayList();
        for (final UserKeyword keyword : keywords) {
            executables.addAll(keyword.getKeywordExecutionRows());
        }
        return executables;
    }

    private void reportUnknownVariables(final IFile file, final List<UserKeyword> keywords) {
        final Set<String> variables = validationContext.getAccessibleVariables();
        final VariableExtractor variableExtractor = new VariableExtractor();
        final String fileName = file.getName();

        for (final UserKeyword keyword : keywords) {
            final Set<String> allVariables = newHashSet(variables);
            allVariables.addAll(extractArgumentVariables(keyword, variableExtractor, fileName));

            TestCasesTableValidator.reportUnknownVariables(file, reporter, validationContext,
                    keyword.getKeywordExecutionRows(), allVariables);
        }
    }

    private Collection<String> extractArgumentVariables(final UserKeyword keyword, final VariableExtractor extractor, final String fileName) {
        final Set<String> arguments = newHashSet();

        // first add arguments embedded in name, then from [Arguments] setting
        arguments.addAll(newArrayList(transform(VariableNamesSupport.extractUnifiedVariableNamesFromArguments(
                newArrayList(keyword.getKeywordName()), extractor, fileName), removeRegex())));
        for (final KeywordArguments argument : keyword.getArguments()) {
            arguments.addAll(VariableNamesSupport.extractUnifiedVariableNamesFromArguments(argument.getArguments(), extractor,
                    fileName));
        }
        return arguments;
    }

    private static Function<String, String> removeRegex() {
        return new Function<String, String>() {

            @Override
            public String apply(final String variable) {
                return removeRegex(variable);
            }
        };
    }

    private static String removeRegex(final String variable) {
        return variable.indexOf(':') != -1 ? variable.substring(0, variable.indexOf(':')) + "}" : variable;
    }
}
