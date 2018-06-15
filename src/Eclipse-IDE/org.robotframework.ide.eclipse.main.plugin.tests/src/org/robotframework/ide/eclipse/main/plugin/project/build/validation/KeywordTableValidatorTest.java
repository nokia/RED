/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.project.build.validation.Contexts.prepareContext;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.Range;

public class KeywordTableValidatorTest {

    @Test
    public void outdatedTableHeaderIsReported() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** User Keywords ***")
                .appendLine("keyword 1")
                .appendLine("  [Return]  42")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsExactly(
                new Problem(KeywordsProblem.USER_KEYWORD_TABLE_HEADER_SYNONYM,
                        new ProblemPosition(1, Range.closed(0, 21))));
    }

    @Test
    public void keywordsAreReported_whenTheyAreDuplicated_1() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword 1")
                .appendLine("  [Return]  42")
                .appendLine("keyword 1")
                .appendLine("  [Return]  100")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsExactly(
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD, new ProblemPosition(2, Range.closed(17, 26))),
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD, new ProblemPosition(4, Range.closed(42, 51))));
    }

    @Test
    public void keywordsAreReported_whenTheyAreDuplicated_2() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword 1")
                .appendLine("  [Return]  1")
                .appendLine("k e y w o r d 1")
                .appendLine("  [Return]  2")
                .appendLine("k_E_y_W_o_R_d_1")
                .appendLine("  [Return]  3")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).containsExactly(
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD, new ProblemPosition(2, Range.closed(17, 26))),
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD, new ProblemPosition(4, Range.closed(41, 56))),
                new Problem(KeywordsProblem.DUPLICATED_KEYWORD, new ProblemPosition(6, Range.closed(71, 86))));
    }

    @Test
    public void noProblemsReported_whenKeywordsTableIsValid() throws CoreException {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword 1")
                .appendLine("  [Return]  1")
                .appendLine("keyword 2)")
                .appendLine("  [Return]  2")
                .appendLine("keyword 3")
                .appendLine("  [Return]  3")
                .build();

        final Collection<Problem> problems = validate(prepareContext(), fileModel);
        assertThat(problems).isEmpty();
    }

    private Collection<Problem> validate(final FileValidationContext context, final RobotSuiteFile fileModel)
            throws CoreException {
        final MockReporter reporter = new MockReporter();
        new KeywordTableValidator(context, fileModel.findSection(RobotKeywordsSection.class), reporter)
                .validate(new NullProgressMonitor());
        return reporter.getReportedProblems();
    }
}
