/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

class KeywordTableValidator implements ModelUnitValidator {

    private final ValidationContext context;

    private final Optional<RobotKeywordsSection> keywordSection;

    private final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();

    KeywordTableValidator(final ValidationContext validationContext,
            final Optional<RobotKeywordsSection> keywordSection) {
        this.context = validationContext;
        this.keywordSection = keywordSection;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!keywordSection.isPresent()) {
            return;
        }
        final RobotSuiteFile suiteModel = keywordSection.get().getSuiteFile();
        final KeywordTable keywordTable = (KeywordTable) keywordSection.get().getLinkedElement();

        reportDuplicatedKewords(suiteModel.getFile(), keywordTable);
        TestCasesTableValidator.reportUnkownKeywords(suiteModel, reporter, findExecutableRows(keywordTable));
    }

    private List<RobotExecutableRow<?>> findExecutableRows(final KeywordTable keywordTable) {
        final List<RobotExecutableRow<?>> executables = newArrayList();
        for (final UserKeyword keyword : keywordTable.getKeywords()) {
            executables.addAll(keyword.getKeywordExecutionRows());
        }
        return executables;
    }

    private void reportDuplicatedKewords(final IFile file, final KeywordTable keywordTable) {
        final Set<String> duplicatedNames = newHashSet();

        for (final UserKeyword kw1 : keywordTable.getKeywords()) {
            for (final UserKeyword kw2 : keywordTable.getKeywords()) {
                if (kw1 != kw2) {
                    final RobotToken kw1Token = kw1.getKeywordName();
                    final String kw1Name = kw1Token.getText().toString();
                    final String kw2Name = kw2.getKeywordName().getText().toString();

                    if (kw1Name.equalsIgnoreCase(kw2Name)) {
                        duplicatedNames.add(kw1Name.toLowerCase());
                    }
                }
            }
        }

        for (final UserKeyword keyword : keywordTable.getKeywords()) {
            final RobotToken keywordName = keyword.getKeywordName();
            final String name = keywordName.getText().toString();

            if (duplicatedNames.contains(name.toLowerCase())) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.DUPLICATED_KEYWORD)
                        .formatMessageWith(name);
                final ProblemPosition position = new ProblemPosition(keywordName.getLineNumber(),
                        Range.closed(keywordName.getStartOffset(), keywordName.getStartOffset() + name.length()));
                final Map<String, Object> additionalArguments = Maps.newHashMap();
                additionalArguments.put("name", name);
                reporter.handleProblem(problem, file, position, additionalArguments);
            }
        }
    }
}
