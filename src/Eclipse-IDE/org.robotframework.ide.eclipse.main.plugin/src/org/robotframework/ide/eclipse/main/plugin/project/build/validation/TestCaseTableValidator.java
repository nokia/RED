/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.ERowType;
import org.rf.ide.core.testdata.model.table.exec.descs.RobotAction;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration.Number;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopContinueRowDescriptor;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport.NameTransformation;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseSetup;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTags;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTeardown;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTimeout;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.ValidationKeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.testcases.DocumentationTestCaseDeclarationSettingValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.testcases.PostconditionDeclarationExistanceValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.testcases.PreconditionDeclarationExistanceValidator;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Range;

class TestCaseTableValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final Optional<RobotCasesSection> testCaseSection;

    private final ProblemsReportingStrategy reporter;

    TestCaseTableValidator(final FileValidationContext validationContext, final Optional<RobotCasesSection> section,
            final ProblemsReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.testCaseSection = section;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!testCaseSection.isPresent()) {
            return;
        }
        final RobotCasesSection robotCasesSection = testCaseSection.get();
        final TestCaseTable casesTable = robotCasesSection.getLinkedElement();
        final List<TestCase> cases = casesTable.getTestCases();

        validateByExternal(robotCasesSection, monitor);

        reportEmptyNamesOfCases(cases);
        reportEmptyCases(cases);
        reportDuplicatedCases(cases);
        reportSettingsProblems(cases);
        reportKeywordUsageProblems(robotCasesSection.getChildren());
        reportUnknownVariables(cases);
    }

    private void validateByExternal(final RobotCasesSection section, final IProgressMonitor monitor)
            throws CoreException {
        new DocumentationTestCaseDeclarationSettingValidator(validationContext.getFile(), section, reporter)
                .validate(monitor);
        new PreconditionDeclarationExistanceValidator(validationContext.getFile(), reporter, section).validate(monitor);
        new PostconditionDeclarationExistanceValidator(validationContext.getFile(), reporter, section)
                .validate(monitor);
    }

    private void reportEmptyNamesOfCases(final List<TestCase> cases) {
        for (final TestCase testCase : cases) {
            final RobotToken caseName = testCase.getName();
            if (caseName.getText().trim().isEmpty()) {
                final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.EMPTY_CASE_NAME);
                final int startOffset = caseName.getStartOffset();
                final int endOffset = testCase.getEndPosition().getOffset();

                final ProblemPosition problemPosition = new ProblemPosition(caseName.getFilePosition().getLine(),
                        Range.closed(startOffset, endOffset));
                reporter.handleProblem(problem, validationContext.getFile(), problemPosition);
            }
        }
    }

    private void reportEmptyCases(final List<TestCase> cases) {
        for (final TestCase testCase : cases) {
            final RobotToken caseName = testCase.getTestName();

            if (!hasAnythingToExecute(testCase)) {
                final String name = caseName.getText();
                final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.EMPTY_CASE).formatMessageWith(name);
                final Map<String, Object> arguments = ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.NAME,
                        name);
                reporter.handleProblem(problem, validationContext.getFile(), caseName, arguments);
            }
        }
    }

    private boolean hasAnythingToExecute(final TestCase testCase) {
        for (final RobotExecutableRow<?> robotExecutableRow : testCase.getTestExecutionRows()) {
            if (robotExecutableRow.isExecutable()) {
                return true;
            }
        }
        return false;
    }

    private void reportDuplicatedCases(final List<TestCase> cases) {
        final Set<String> duplicatedNames = newHashSet();

        for (final TestCase case1 : cases) {
            for (final TestCase case2 : cases) {
                if (case1 != case2) {
                    final String case1Name = case1.getTestName().getText();
                    final String case2Name = case2.getTestName().getText();

                    if (case1Name.equalsIgnoreCase(case2Name)) {
                        duplicatedNames.add(case1Name.toLowerCase());
                    }
                }
            }
        }

        for (final TestCase testCase : cases) {
            final RobotToken caseName = testCase.getTestName();
            final String name = caseName.getText();

            if (duplicatedNames.contains(name.toLowerCase())) {
                final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.DUPLICATED_CASE)
                        .formatMessageWith(name);
                final Map<String, Object> additionalArguments = ImmutableMap.<String, Object> of("name", name);
                reporter.handleProblem(problem, validationContext.getFile(), caseName, additionalArguments);
            }
        }
    }

    private void reportSettingsProblems(final List<TestCase> cases) throws CoreException {
        for (final TestCase testCase : cases) {
            new TestCaseSettingsValidator(validationContext, testCase, reporter).validate(null);
        }
    }

    void reportKeywordUsageProblems(final List<RobotCase> cases) {
        for (final RobotCase testCase : cases) {
            reportKeywordUsageProblemsInTestCaseSettings(testCase);
            reportKeywordUsageProblems(validationContext, reporter, testCase.getLinkedElement().getTestExecutionRows(),
                    testCase.getTemplateInUse());
        }
        
    }
    
    private void reportKeywordUsageProblemsInTestCaseSettings(final RobotCase testCase) {
        final RobotToken templateKeywordToken = testCase.getLinkedElement().getTemplateKeywordLocation();
        if (templateKeywordToken != null && !templateKeywordToken.getFilePosition().isNotSet()
                && isTemplateFromTestCasesTable(testCase)
                && !templateKeywordToken.getText().toLowerCase().equals("none")) {
            validateExistingKeywordCall(validationContext, reporter, templateKeywordToken,
                    Optional.<List<RobotToken>> absent());
        }
    }

    static void reportKeywordUsageProblemsInSetupAndTeardownSetting(final FileValidationContext validationContext,
            final ProblemsReportingStrategy reporter, final RobotToken keywordNameToken,
            final Optional<List<RobotToken>> arguments) {
        final MappingResult variablesExtraction = new VariableExtractor().extract(keywordNameToken,
                validationContext.getFile().getName());
        final List<VariableDeclaration> variablesDeclarations = variablesExtraction.getCorrectVariables();
        if (variablesExtraction.getMappedElements().size() == 1 && variablesDeclarations.size() == 1) {
            final RobotProblem problem = RobotProblem
                    .causedBy(GeneralSettingsProblem.VARIABLE_AS_KEYWORD_USAGE_IN_SETTING)
                    .formatMessageWith(variablesDeclarations.get(0).getVariableName().getText());
            reporter.handleProblem(problem, validationContext.getFile(), keywordNameToken);
        } else {
            validateExistingKeywordCall(validationContext, reporter, keywordNameToken, arguments);
        }
    }

    static void reportKeywordUsageProblems(final FileValidationContext validationContext,
            final ProblemsReportingStrategy reporter, final List<? extends RobotExecutableRow<?>> executables,
            final Optional<String> templateKeyword) {

        for (final RobotExecutableRow<?> executable : executables) {
            if (!executable.isExecutable() || templateKeyword.isPresent()) {
                continue;
            }

            final IExecutableRowDescriptor<?> executableRowDescriptor = executable.buildLineDescription();
            RobotToken keywordName = executableRowDescriptor.getAction().getToken();

            final IFile file = validationContext.getFile();
            if (executableRowDescriptor.getRowType() == ERowType.FOR) {
                final List<BuildMessage> messages = executableRowDescriptor.getMessages();
                for (final BuildMessage buildMessage : messages) {
                    final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.UNKNOWN_KEYWORD)
                            .formatMessageWith(buildMessage.getMessage());
                    reporter.handleProblem(problem, file, keywordName);
                }
                continue;
            }

            if (executableRowDescriptor.getRowType() == ERowType.FOR_CONTINUE) {
                final ForLoopContinueRowDescriptor<?> loopContinueRowDescriptor = (ForLoopContinueRowDescriptor<?>) executable
                        .buildLineDescription();
                keywordName = loopContinueRowDescriptor.getKeywordAction().getToken();
            }

            if (!keywordName.getFilePosition().isNotSet()) {
                validateExistingKeywordCall(validationContext, reporter, keywordName,
                        Optional.of(executableRowDescriptor.getKeywordArguments()));
            } else {
                reporter.handleProblem(RobotProblem.causedBy(KeywordsProblem.MISSING_KEYWORD)
                        .formatMessageWith(executable.getAction().getText()), file, executable.getAction());
            }
        }
    }

    static void validateExistingKeywordCall(final FileValidationContext validationContext,
            final ProblemsReportingStrategy reporter, final RobotToken keywordName,
            final Optional<List<RobotToken>> arguments) {
        final Optional<String> nameToUse = GherkinStyleSupport.firstNameTransformationResult(keywordName.getText(),
                new NameTransformation<String>() {

                    @Override
                    public Optional<String> transform(final String gherkinNameVariant) {
                        return validationContext.isKeywordAccessible(gherkinNameVariant)
                                ? Optional.of(gherkinNameVariant) : Optional.<String> absent();
                    }
                });
        final String name = !nameToUse.isPresent() || nameToUse.get().isEmpty() ? keywordName.getText()
                : nameToUse.get();
        final int offset = keywordName.getStartOffset() + (keywordName.getText().length() - name.length());
        final ProblemPosition position = new ProblemPosition(keywordName.getLineNumber(),
                Range.closed(offset, offset + name.length()));

        if (!nameToUse.isPresent()) {
            reporter.handleProblem(RobotProblem.causedBy(KeywordsProblem.UNKNOWN_KEYWORD).formatMessageWith(name),
                    validationContext.getFile(), position, ImmutableMap.<String, Object> of(
                            AdditionalMarkerAttributes.NAME, name, AdditionalMarkerAttributes.ORIGINAL_NAME,
                            keywordName.getText()));
            return;
        }

        final ListMultimap<KeywordScope, KeywordEntity> keywords = validationContext.getPossibleKeywords(name);

        for (final KeywordScope scope : KeywordScope.defaultOrder()) {
            final List<KeywordEntity> keywordEntities = keywords.get(scope);
            if (keywordEntities.size() == 1) {
                final ValidationKeywordEntity keyword = (ValidationKeywordEntity) keywordEntities.get(0);
                if (keyword.isDeprecated()) {
                    reporter.handleProblem(
                            RobotProblem.causedBy(KeywordsProblem.DEPRECATED_KEYWORD).formatMessageWith(name),
                            validationContext.getFile(), position);
                }
                if (keyword.isFromNestedLibrary(validationContext.getFile())) {
                    reporter.handleProblem(
                            RobotProblem.causedBy(KeywordsProblem.KEYWORD_FROM_NESTED_LIBRARY).formatMessageWith(name),
                            validationContext.getFile(), position);
                }
                if (keyword.hasInconsistentName(name)) {
                    reporter.handleProblem(
                            RobotProblem.causedBy(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION)
                                    .formatMessageWith(name, keyword.getNameFromDefinition()),
                            validationContext.getFile(), position,
                            ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.NAME, name,
                                    AdditionalMarkerAttributes.ORIGINAL_NAME, keyword.getNameFromDefinition(),
                                    AdditionalMarkerAttributes.SOURCES, keyword.getSourceNameInUse()));
                }
                if (arguments.isPresent()) {
                    new KeywordCallArgumentsValidator(validationContext.getFile(), keywordName, reporter,
                            keyword.getArgumentsDescriptor(), arguments.get()).validate(new NullProgressMonitor());
                }
                break;
            } else if (keywordEntities.size() > 1) {
                final Iterable<?> sources = transform(keywordEntities, new Function<KeywordEntity, String>() {

                    @Override
                    public String apply(final KeywordEntity kw) {
                        return kw.getSourceNameInUse();
                    }
                });
                reporter.handleProblem(
                        RobotProblem.causedBy(KeywordsProblem.AMBIGUOUS_KEYWORD).formatMessageWith(name,
                                "[" + Joiner.on(", ").join(sources) + "]"),
                        validationContext.getFile(), position,
                        ImmutableMap.<String, Object> of(AdditionalMarkerAttributes.NAME, name,
                                AdditionalMarkerAttributes.ORIGINAL_NAME, keywordName.getText(),
                                AdditionalMarkerAttributes.SOURCES, Joiner.on(';').join(sources)));
                break;
            }
        }
    }

    private void reportUnknownVariables(final List<TestCase> cases) {
        final Set<String> variables = validationContext.getAccessibleVariables();

        for (final TestCase testCase : cases) {
            reportUnknownVariablesInTimeoutSetting(testCase, variables);
            reportUnknownVariablesInTagsSetting(testCase, variables);
            reportUnknownVariables(validationContext, reporter, collectTestCaseExeRowsForVariablesChecking(testCase), variables);
        }
    }
    
    private List<? extends RobotExecutableRow<?>> collectTestCaseExeRowsForVariablesChecking(final TestCase testCase) {
        final List<RobotExecutableRow<?>> exeRows = newArrayList();
        final List<TestCaseSetup> setups = testCase.getSetups();
        if (!setups.isEmpty()) {
            exeRows.add(setups.get(0).asExecutableRow());
        }
        exeRows.addAll(testCase.getTestExecutionRows());
        final List<TestCaseTeardown> teardowns = testCase.getTeardowns();
        if (!teardowns.isEmpty()) {
            exeRows.add(teardowns.get(0).asExecutableRow());
        }
        return exeRows;
    }
    
    private void reportUnknownVariablesInTimeoutSetting(final TestCase testCase, final Set<String> variables) {
        final List<TestCaseTimeout> timeouts = testCase.getTimeouts();
        for (final TestCaseTimeout testCaseTimeout : timeouts) {
            final RobotToken timeoutToken = testCaseTimeout.getTimeout();
            if (timeoutToken != null) {
                validateTimeoutSetting(validationContext, reporter, variables, timeoutToken);
            }
        }
    }
    
    static void validateTimeoutSetting(final FileValidationContext validationContext,
            final ProblemsReportingStrategy reporter, final Set<String> variables, final RobotToken timeoutToken) {
        final String timeout = timeoutToken.getText();
        if (!RobotTimeFormat.isValidRobotTimeArgument(timeout.trim())) {
            final List<VariableDeclaration> variablesDeclarations = new VariableExtractor()
                    .extract(timeoutToken, validationContext.getFile().getName()).getCorrectVariables();
            if (!variablesDeclarations.isEmpty()) {
                reportUnknownVariablesInSettingWithoutExeRows(validationContext, reporter, variablesDeclarations, variables);
            } else {
                final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_TIME_FORMAT)
                        .formatMessageWith(timeout);
                reporter.handleProblem(problem, validationContext.getFile(), timeoutToken);
            }
        }
    }
    
    private void reportUnknownVariablesInTagsSetting(final TestCase testCase, final Set<String> variables) {
        final List<TestCaseTags> tags = testCase.getTags();
        for (final TestCaseTags testCaseTags : tags) {
            final List<RobotToken> tagsTokens = testCaseTags.getTags();
            for (final RobotToken tagToken : tagsTokens) {
                final List<VariableDeclaration> variablesDeclarationsInTag = new VariableExtractor()
                        .extract(tagToken, validationContext.getFile().getName()).getCorrectVariables();
                if (!variablesDeclarationsInTag.isEmpty()) {
                    reportUnknownVariablesInSettingWithoutExeRows(validationContext, reporter, variablesDeclarationsInTag, variables);
                }
            }
        }
    }
    
    static void reportUnknownVariablesInSettingWithoutExeRows(final FileValidationContext validationContext,
            final ProblemsReportingStrategy reporter, final List<VariableDeclaration> variablesDeclarations,
            final Set<String> variables) {
        final Set<String> definedVariables = newHashSet(variables);
        for (final VariableDeclaration variableDeclaration : variablesDeclarations) {
            if (TestCaseTableValidator.isInvalidVariableDeclaration(definedVariables, variableDeclaration)) {
                final String variableName = variableDeclaration.getVariableName().getText();
                final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.UNDECLARED_VARIABLE_USE)
                        .formatMessageWith(variableName);
                final int variableOffset = variableDeclaration.getStartFromFile().getOffset();
                final ProblemPosition position = new ProblemPosition(variableDeclaration.getStartFromFile().getLine(),
                        Range.closed(variableOffset, variableOffset
                                + ((variableDeclaration.getEndFromFile().getOffset() + 1) - variableOffset)));
                final Map<String, Object> additionalArguments = ImmutableMap
                        .<String, Object> of(AdditionalMarkerAttributes.NAME, variableDeclaration.asToken().getText());
                reporter.handleProblem(problem, validationContext.getFile(), position, additionalArguments);
            }
        }
    }

    static void reportUnknownVariables(final FileValidationContext validationContext,
            final ProblemsReportingStrategy reporter, final List<? extends RobotExecutableRow<?>> executables,
            final Set<String> variables) {

        final Set<String> definedVariables = newHashSet(variables);

        for (final RobotExecutableRow<?> row : executables) {
            if (row.isExecutable()) {
                final IExecutableRowDescriptor<?> lineDescription = row.buildLineDescription();

                for (final VariableDeclaration variableDeclaration : lineDescription.getUsedVariables()) {
                    if (isInvalidVariableDeclaration(validationContext, definedVariables, lineDescription, variableDeclaration)) {
                        final String variableName = variableDeclaration.getVariableName().getText();
                        final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.UNDECLARED_VARIABLE_USE)
                                .formatMessageWith(variableName);
                        final int variableOffset = variableDeclaration.getStartFromFile().getOffset();
                        final ProblemPosition position = new ProblemPosition(
                                variableDeclaration.getStartFromFile().getLine(),
                                Range.closed(variableOffset, variableOffset
                                        + ((variableDeclaration.getEndFromFile().getOffset() + 1) - variableOffset)));
                        final Map<String, Object> additionalArguments = ImmutableMap.<String, Object> of(
                                AdditionalMarkerAttributes.NAME, variableDeclaration.asToken().getText());
                        reporter.handleProblem(problem, validationContext.getFile(), position, additionalArguments);
                    }
                }
                definedVariables.addAll(
                        VariableNamesSupport.extractUnifiedVariableNames(lineDescription.getCreatedVariables()));
            }
        }
    }
    
    static boolean isInvalidVariableDeclaration(final Set<String> definedVariables,
            final VariableDeclaration variableDeclaration) {
        return !variableDeclaration.isDynamic()
                && !VariableNamesSupport.isDefinedVariable(variableDeclaration, definedVariables)
                && !isSpecificVariableDeclaration(definedVariables, variableDeclaration);
    }

    static boolean isInvalidVariableDeclaration(final FileValidationContext validationContext,
            final Set<String> definedVariables, final IExecutableRowDescriptor<?> lineDescription,
            final VariableDeclaration variableDeclaration) {
        return isInvalidVariableDeclaration(definedVariables, variableDeclaration)
                && !isVariableInSetterOrGetterOrCommentKeyword(validationContext, definedVariables, lineDescription, variableDeclaration);
    }

    private static boolean isSpecificVariableDeclaration(final Set<String> definedVariables,
            final VariableDeclaration variableDeclaration) {
        return variableDeclaration.getVariableType() instanceof Number
                || VariableNamesSupport.isDefinedVariableInsideComputation(variableDeclaration, definedVariables);
    }

    private static boolean isVariableInSetterOrGetterOrCommentKeyword(final FileValidationContext validationContext,
            final Set<String> definedVariables, final IExecutableRowDescriptor<?> lineDescription,
            final VariableDeclaration variableDeclaration) {
        final String keywordName = QualifiedKeywordName.fromOccurrence(getKeyword(lineDescription)).getKeywordName();
        if (keywordName.equals("settestvariable") && isKeywordFromBuiltin(validationContext, keywordName)) {
            final List<VariableDeclaration> usedVariables = lineDescription.getUsedVariables();
            if (!usedVariables.isEmpty()) {
                definedVariables.add(VariableNamesSupport.extractUnifiedVariableName(usedVariables.get(0).asToken().getText()));
                return true;
            }
        } else if(keywordName.equals("getvariablevalue") && isKeywordFromBuiltin(validationContext, keywordName)) {
            final List<VariableDeclaration> usedVariables = lineDescription.getUsedVariables();
            if (!usedVariables.isEmpty() && usedVariables.get(0).equals(variableDeclaration)) {
                return true;
            }
        } else if (keywordName.equals("comment") && isKeywordFromBuiltin(validationContext, keywordName)) {
            return true;
        }
        return false;
    }

    private static boolean isKeywordFromBuiltin(final FileValidationContext validationContext,
            final String keywordName) {

        for (final KeywordScope scope : KeywordScope.defaultOrder()) {
            final List<KeywordEntity> possible = validationContext.getPossibleKeywords(keywordName).get(scope);
            if (scope != KeywordScope.STD_LIBRARY && !possible.isEmpty()) {
                return false;
            } else if (scope == KeywordScope.STD_LIBRARY) {
                return possible.size() == 1 && possible.get(0).getSourceNameInUse().equals("BuiltIn");
            }
        }
        return false;
    }

    private static String getKeyword(final IExecutableRowDescriptor<?> lineDescription) {
        final RobotAction action;
        if (lineDescription.getRowType() == ERowType.FOR_CONTINUE) {
            action = ((ForLoopContinueRowDescriptor<?>) lineDescription).getKeywordAction();
        } else {
            action = lineDescription.getAction();
        }
        return action.getToken().getText();
    }
    
    private boolean isTemplateFromTestCasesTable(final RobotCase testCase) {
    	return testCase.getLinkedElement().getRobotViewAboutTestTemplate() != null;
    }
}
