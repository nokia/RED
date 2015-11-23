/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.executableDescriptors.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.executableDescriptors.IExecutableRowDescriptor.ERowType;
import org.rf.ide.core.testdata.model.table.executableDescriptors.VariableExtractor;
import org.rf.ide.core.testdata.model.table.executableDescriptors.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.executableDescriptors.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.executableDescriptors.impl.ForLoopContinueRowDescriptor;
import org.rf.ide.core.testdata.model.table.testCases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

class TestCasesTableValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final Optional<RobotCasesSection> testCaseSection;

    private final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();

    TestCasesTableValidator(final FileValidationContext validationContext, final Optional<RobotCasesSection> section) {
        this.validationContext = validationContext;
        this.testCaseSection = section;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!testCaseSection.isPresent()) {
            return;
        }
        final RobotSuiteFile suiteModel = testCaseSection.get().getSuiteFile();
        final TestCaseTable casesTable = (TestCaseTable) testCaseSection.get().getLinkedElement();
        final List<TestCase> cases = casesTable.getTestCases();

        reportEmptyCases(suiteModel.getFile(), cases);
        reportDuplicatedCases(suiteModel.getFile(), cases);
        reportKeywordUsageProblems(suiteModel, validationContext, reporter, findExecutableRows(cases));
        reportUnknownVariables(suiteModel, cases);
    }

    private void reportEmptyCases(final IFile file, final List<TestCase> cases) {
        for (final TestCase testCase : cases) {
            final RobotToken caseName = testCase.getTestName();
            final IProblemCause cause = TestCasesProblem.EMPTY_CASE;
            reportEmptyExecutableRows(file, reporter, caseName, testCase.getTestExecutionRows(), cause);
        }
    }

    static void reportEmptyExecutableRows(final IFile file, final ProblemsReportingStrategy reporter,
            final RobotToken def, final List<? extends RobotExecutableRow<?>> executables,
            final IProblemCause causeToReport) {
        if (executables.isEmpty()) {
            final String name = def.getText().toString();
            final RobotProblem problem = RobotProblem.causedBy(causeToReport).formatMessageWith(name);
            final Map<String, Object> arguments = ImmutableMap.<String, Object> of("name", name);
            reporter.handleProblem(problem, file, def, arguments);
        }
    }

    private void reportDuplicatedCases(final IFile file, final List<TestCase> cases) {
        final Set<String> duplicatedNames = newHashSet();

        for (final TestCase case1 : cases) {
            for (final TestCase case2 : cases) {
                if (case1 != case2) {
                    final String case1Name = case1.getTestName().getText().toString();
                    final String case2Name = case2.getTestName().getText().toString();

                    if (case1Name.equalsIgnoreCase(case2Name)) {
                        duplicatedNames.add(case1Name.toLowerCase());
                    }
                }
            }
        }

        for (final TestCase testCase : cases) {
            final RobotToken caseName = testCase.getTestName();
            final String name = caseName.getText().toString();

            if (duplicatedNames.contains(name.toLowerCase())) {
                final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.DUPLICATED_CASE)
                        .formatMessageWith(name);
                final Map<String, Object> additionalArguments = ImmutableMap.<String, Object> of("name", name);
                reporter.handleProblem(problem, file, caseName, additionalArguments);
            }
        }

    }

    private static List<RobotExecutableRow<?>> findExecutableRows(final List<TestCase> cases) {
        final List<RobotExecutableRow<?>> executables = newArrayList();
        for (final TestCase testCase : cases) {
            executables.addAll(testCase.getTestExecutionRows());
        }
        return executables;
    }

    static void reportKeywordUsageProblems(final RobotSuiteFile robotSuiteFile, final FileValidationContext validationContext,
            final ProblemsReportingStrategy reporter, final List<RobotExecutableRow<?>> executables) {
        
        for (final RobotExecutableRow<?> executable : executables) {
            
            if (!executable.isExecutable()) {
                continue;
            }
            
            final IExecutableRowDescriptor<?> executableRowDescriptor = executable.buildLineDescription();
            RobotToken keywordName = executableRowDescriptor.getAction().getToken();
            
            if (executableRowDescriptor.getRowType() == ERowType.FOR) {
                final List<BuildMessage> messages = executableRowDescriptor.getMessages();
                for (final BuildMessage buildMessage : messages) {
                    reportKeywordProblem(reporter, KeywordsProblem.UNKNOWN_KEYWORD, buildMessage.getMessage(),
                            robotSuiteFile.getFile(), keywordName);
                }
                continue;
            }
            
            if (executableRowDescriptor.getRowType() == ERowType.FOR_CONTINUE) {
                final ForLoopContinueRowDescriptor<?> loopContinueRowDescriptor = (ForLoopContinueRowDescriptor<?>) executable.buildLineDescription();
                keywordName = loopContinueRowDescriptor.getKeywordAction().getToken();
            }
            
            if (!keywordName.getFilePosition().isNotSet()) {
                final String name = keywordName.getText().toString();
                
                if (!validationContext.isKeywordAccessible(name)) {
                    reportKeywordProblemWithArguments(reporter, KeywordsProblem.UNKNOWN_KEYWORD, name,
                            robotSuiteFile.getFile(), keywordName, ImmutableMap.<String, Object> of("name", name));
                }
                if (validationContext.isKeywordDeprecated(name)) {
                    reportKeywordProblem(reporter, KeywordsProblem.DEPRECATED_KEYWORD, name,
                            robotSuiteFile.getFile(), keywordName);
                }
                if (validationContext.isKeywordFromNestedLibrary(name)) {
                    reportKeywordProblem(reporter, KeywordsProblem.KEYWORD_FROM_NESTED_LIBRARY, name,
                            robotSuiteFile.getFile(), keywordName);
                }
            } else {
                reportKeywordProblem(reporter, KeywordsProblem.UNKNOWN_KEYWORD, executable.getAction().getText().toString(), 
                        robotSuiteFile.getFile(), executable.getAction());
            }
        }
    }
    
    private static void reportKeywordProblem(final ProblemsReportingStrategy reporter, final IProblemCause cause,
            final String message, final IFile file, final RobotToken token) {
        reportKeywordProblemWithArguments(reporter, cause, message, file, token, new HashMap<String, Object>());
    }

    private static void reportKeywordProblemWithArguments(final ProblemsReportingStrategy reporter,
            final IProblemCause cause, final String message, final IFile file, final RobotToken token,
            final Map<String, Object> arguments) {
        final RobotProblem problem = RobotProblem.causedBy(cause).formatMessageWith(message);
        reporter.handleProblem(problem, file, token, arguments);
    }

    private void reportUnknownVariables(final RobotSuiteFile suiteModel,
            final List<TestCase> cases) {        
        final Set<String> variables = validationContext.getAccessibleVariables();

        for (final TestCase testCase : cases) {
            reportUnknownVariables(suiteModel.getFile(), reporter, testCase.getTestExecutionRows(), variables);
        }
    }

    static void reportUnknownVariables(final IFile file, final ProblemsReportingStrategy reporter,
            final List<? extends RobotExecutableRow<?>> executables, final Set<String> variables) {
        
        final Set<String> definedVariables = newHashSet(variables);

        for (final RobotExecutableRow<?> row : executables) {
            if (row.isExecutable()) {
                final IExecutableRowDescriptor<?> lineDescription = row.buildLineDescription();

                for (VariableDeclaration variableDeclaration : lineDescription.getUsedVariables()) {
                    if (!isDefinedVariable(variableDeclaration, definedVariables)) {
                        final String variableName = variableDeclaration.getVariableName().getText();
                        final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.UNDECLARED_VARIABLE_USE)
                                .formatMessageWith(variableName);
                        final int variableOffset = variableDeclaration.getStartFromFile().getOffset();
                        final ProblemPosition position = new ProblemPosition(variableDeclaration.getStartFromFile()
                                .getLine(), Range.closed(variableOffset, variableOffset
                                + ((variableDeclaration.getEndFromFile().getOffset() + 1) - variableOffset)));
                        final Map<String, Object> additionalArguments = ImmutableMap.<String, Object> of("name",
                                variableName);
                        reporter.handleProblem(problem, file, position, additionalArguments);
                    }
                }
                definedVariables.addAll(extractVariableNames(lineDescription.getCreatedVariables()));
            }
        }
    }
    
    private static boolean isDefinedVariable(final VariableDeclaration variableDeclaration, final Set<String> definedVariables) {
        final String varTypeIdentificator = variableDeclaration.getTypeIdentificator().getText();
        final String varNameWithBrackets = createVariableNameWithBrackets(variableDeclaration.getVariableName()
                .getText()
                .toLowerCase());
        if (containsVariable(definedVariables, varTypeIdentificator, varNameWithBrackets)
                || (varNameWithBrackets.contains(".") && containsVariable(definedVariables, varTypeIdentificator,
                        extractVarNameFromDotsRepresentation(varNameWithBrackets)))) {
            return true;
        }
        return false;
    }

    private static boolean containsVariable(final Set<String> definedVariables, final String varTypeIdentificator,
            final String varNameWithBrackets) {
        return definedVariables.contains(varTypeIdentificator + varNameWithBrackets)
                || (!varTypeIdentificator.equals("@") && definedVariables.contains("@" + varNameWithBrackets))
                || (!varTypeIdentificator.equals("&") && definedVariables.contains("&" + varNameWithBrackets))
                || (!varTypeIdentificator.equals("$") && definedVariables.contains("$" + varNameWithBrackets));
    }
    
    private static String extractVarNameFromDotsRepresentation(final String varNameWithBrackets) {
        return varNameWithBrackets.split("\\.")[0] + "}";
    }
    
    @VisibleForTesting
    static List<String> extractVariableNames(final List<VariableDeclaration> assignments) {
        final List<String> vars = newArrayList();
        for (final VariableDeclaration variableDeclaration : assignments) {
            vars.add(variableDeclaration.asToken().getText().toString().toLowerCase());
        }
        return vars;
    }
    
    static List<String> extractVariableNamesFromArguments(final List<RobotToken> assignments,
            final VariableExtractor extractor, final String fileName) {
        final List<String> vars = newArrayList();
        for (RobotToken token : assignments) {
            final MappingResult mappingResult = extractor.extract(token, fileName);
            vars.addAll(extractVariableNames(mappingResult.getCorrectVariables()));
        }
        return vars;
    }
    
    private static String createVariableNameWithBrackets(final String varName) {
        return "{" + varName + "}";
    }
}
