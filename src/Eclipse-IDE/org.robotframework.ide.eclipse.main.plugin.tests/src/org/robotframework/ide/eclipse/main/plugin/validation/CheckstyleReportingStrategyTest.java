/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;

import org.assertj.core.util.Files;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy.ReportingInterruptedException;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;
import org.robotframework.red.junit.jupiter.RedTempDirectory;

import com.google.common.base.Charsets;

@ExtendWith(RedTempDirectory.class)
public class CheckstyleReportingStrategyTest {

    @TempDir
    File tempFolder;

    @Test
    public void whenProjectIsValidated_normalStrategyLogsInfoAboutTimeAndReportedProblems() {
        final Logger logger = mock(Logger.class);
        final CheckstyleReportingStrategy strategy = new CheckstyleReportingStrategy(false, "report.xml", logger);

        strategy.projectValidationStarted("p");
        strategy.projectValidationFinished("p");

        verify(logger).log(
                matches("Project p validation has FINISHED \\(took \\d[.,]\\d{3} seconds and found 0 problems\\)"));
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void whenProjectIsValidated_panickingStrategyDoesNotLogAnythingIfThereAreNoProblemsFound() {
        final Logger logger = mock(Logger.class);
        final CheckstyleReportingStrategy strategy = new CheckstyleReportingStrategy(true, "report.xml", logger);

        strategy.projectValidationStarted("p");
        strategy.projectValidationFinished("p");

        verifyNoInteractions(logger);
    }

    @Test
    public void whenProjectIsValidated_panickingStrategyLogsInfoIfThereAreProblemsFound() {
        final Logger logger = mock(Logger.class);
        final CheckstyleReportingStrategy strategy = new CheckstyleReportingStrategy(true, "report.xml", logger);

        strategy.projectValidationStarted("p");
        try {
            strategy.handleProblem(mock(RobotProblem.class), mock(IFile.class), 5);
        } catch (final ReportingInterruptedException e) {
        }
        strategy.projectValidationFinished("p");

        verify(logger).log(
                matches("Project p validation has FINISHED \\(took \\d[.,]\\d{3} seconds and found 1 fatal problems\\)"));
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void whenReportingIsFinished_strategyGeneratesTheReportFile() throws Exception {
        final File file = RedTempDirectory.createNewFile(tempFolder, "my_report.xml");

        final Logger logger = mock(Logger.class);
        final CheckstyleReportingStrategy strategy = new CheckstyleReportingStrategy(false, file.getAbsolutePath(),
                logger);

        final IFile firstFile = mock(IFile.class);
        when(firstFile.getLocation()).thenReturn(new Path("path/to/file"));

        final RobotProblem problem = mock(RobotProblem.class);
        when(problem.getMessage()).thenReturn("some issue found");
        when(problem.getSeverity()).thenReturn(Severity.ERROR);

        strategy.handleProblem(problem, firstFile, 5);
        strategy.finishReporting();

        final String content = Files.contentOf(file, Charsets.UTF_8);

        assertThat(content).contains("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        assertThat(content).contains("<checkstyle");
        assertThat(content).contains("</checkstyle>");

        assertThat(content).contains("<file name=\"path/to/file\">");

        assertThat(content).contains("<error line=\"5\" message=\"some issue found\" severity=\"error\"/>");
    }

}
