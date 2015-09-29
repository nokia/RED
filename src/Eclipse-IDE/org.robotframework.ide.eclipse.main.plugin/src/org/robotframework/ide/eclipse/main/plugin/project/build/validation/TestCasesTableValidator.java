/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

class TestCasesTableValidator implements ModelUnitValidator {

    private final Optional<RobotCasesSection> testCaseSection;

    private final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();

    TestCasesTableValidator(final Optional<RobotCasesSection> section) {
        this.testCaseSection = section;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!testCaseSection.isPresent()) {
            return;
        }
        reportUnkownKeywords(testCaseSection.get().getSuiteFile(), reporter, findExecutableRows());
    }

    private List<RobotExecutableRow<?>> findExecutableRows() {
        final List<RobotExecutableRow<?>> executables = newArrayList();
        final TestCaseTable testCaseTable = (TestCaseTable) testCaseSection.get().getLinkedElement();
        for (final TestCase testCase : testCaseTable.getTestCases()) {
            executables.addAll(testCase.getTestExecutionRows());
        }
        return executables;
    }

    static void reportUnkownKeywords(final RobotSuiteFile robotSuiteFile, final ProblemsReportingStrategy reporter,
            final List<RobotExecutableRow<?>> executables) {
        final Set<String> names = collectAccessibleKeywordNames(robotSuiteFile);

        for (final RobotExecutableRow<?> executable : executables) {
            final RobotToken keywordName = getKeywordNameToken(executable);
            if (keywordName == null) {
                continue;
            }
            final String name = keywordName.getText().toString();
            if (!names.contains(name)) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.UNKNOWN_KEYWORD)
                        .formatMessageWith(name);
                final ProblemPosition position = new ProblemPosition(keywordName.getLineNumber(),
                        Range.closed(keywordName.getStartOffset(), keywordName.getStartOffset() + name.length()));
                final Map<String, Object> additionalArguments = Maps.newHashMap();
                additionalArguments.put("name", name);
                reporter.handleProblem(problem, robotSuiteFile.getFile(), position, additionalArguments);
            }
        }
    }

    private static Set<String> collectAccessibleKeywordNames(final RobotSuiteFile robotSuiteFile) {
        final Set<String> names = new HashSet<>();
        new KeywordDefinitionLocator(robotSuiteFile, false)
                .locateKeywordDefinition(new KeywordDetector() {

                    @Override
                    public ContinueDecision libraryKeywordDetected(final LibrarySpecification libSpec,
                            final KeywordSpecification kwSpec) {
                        names.add(kwSpec.getName());
                        return ContinueDecision.CONTINUE;
                    }

                    @Override
                    public ContinueDecision keywordDetected(final RobotSuiteFile file,
                            final RobotKeywordDefinition keyword) {
                        names.add(keyword.getName());
                        return ContinueDecision.CONTINUE;
                    }
                });
        return names;
    }

    private static RobotToken getKeywordNameToken(final RobotExecutableRow<?> executable) {
        final List<RobotToken> candidates = newArrayList();
        candidates.add(executable.getAction());
        candidates.addAll(executable.getArguments());

        boolean isFirst = true;
        for (final RobotToken token : candidates) {
            if (Pattern.matches("^[@$&%]\\{[^\\{]+\\} *=? *$", token.getText().toString())) {
                isFirst = false;
                continue;
            } else if (isFirst && "\\".equals(token.getText().toString().trim())) {
                isFirst = false;
                continue;
            } else if (isFirst && ": FOR".equals(token.getText().toString().trim())) {
                return null;
            }
            return token;
        }
        return null;
    }
}
