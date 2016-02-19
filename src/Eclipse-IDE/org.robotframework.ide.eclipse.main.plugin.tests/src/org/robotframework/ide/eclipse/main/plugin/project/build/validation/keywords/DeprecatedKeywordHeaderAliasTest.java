/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.keywords;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.Range;

public class DeprecatedKeywordHeaderAliasTest {

    private MockReporter reporter;

    @Before
    public void beforeTest() {
        reporter = new MockReporter();
    }
    
    @Test
    public void sectionHeaderIsReported_whenDeprecatedWordIsUsed() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** User Keywords ***")
                .appendLine("kw")
                .appendLine("    [Return]    1")
                .build();
        
        final RobotKeywordsSection section = file.findSection(RobotKeywordsSection.class).get();

        new DeprecatedKeywordHeaderAlias(file.getFile(), reporter, section).validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(new Problem(
                KeywordsProblem.USER_KEYWORD_TABLE_HEADER_SYNONIM, new ProblemPosition(1, Range.closed(0, 21))));
    }
    
    @Test
    public void nothingIsReported_whenSectionHeaderUsesKeywordsWord() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("    [Return]    1")
                .build();
        
        final RobotKeywordsSection section = file.findSection(RobotKeywordsSection.class).get();

        new DeprecatedKeywordHeaderAlias(file.getFile(), reporter, section).validate(null);

        assertThat(reporter.getReportedProblems()).isEmpty();
    }
}
