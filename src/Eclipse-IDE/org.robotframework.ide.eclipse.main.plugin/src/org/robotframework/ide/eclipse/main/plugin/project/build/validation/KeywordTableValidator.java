/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;

import com.google.common.collect.Range;

class KeywordTableValidator implements ModelUnitValidator {

    private final IFile file;

    private final KeywordTable keywordTable;

    private final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();

    KeywordTableValidator(final IFile file, final KeywordTable keywordTable) {
        this.file = file;
        this.keywordTable = keywordTable;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!keywordTable.isPresent()) {
            return;
        }
        reportDuplicatedKewords(keywordTable);
    }

    private void reportDuplicatedKewords(final KeywordTable keywordTable) {
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
                reporter.handleProblem(problem, file, position);
            }
        }
    }
}
