package org.robotframework.ide.eclipse.main.plugin.validation;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.util.Files;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;
import org.robotframework.ide.eclipse.main.plugin.validation.CheckstyleReportingStrategy.RobotProblemWithPosition;

import com.google.common.base.Charsets;

public class ReportWithCheckstyleFormatTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void emptyCheckstyleFileIsWritten_whenThereAreNoProblems() throws Exception {
        final File file = temporaryFolder.newFile("empty.xml");

        try (ReportWithCheckstyleFormat reporter = new ReportWithCheckstyleFormat(file)) {
            reporter.write(new HashMap<>());
        }
        final String content = Files.contentOf(file, Charsets.UTF_8);

        assertThat(content).contains("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        assertThat(content).contains("<checkstyle");
        assertThat(content).contains("</checkstyle>");
        assertThat(content).doesNotContain("<file");
        assertThat(content).doesNotContain("<error");
    }

    @Test
    public void checkStyleFileWithViolationsIsWritten_whenThereAreProblems() throws Exception {
        final File file = temporaryFolder.newFile("empty.xml");

        final RobotProblem problem1 = mock(RobotProblem.class);
        when(problem1.getMessage()).thenReturn("ImportantProblem!!!");
        when(problem1.getSeverity()).thenReturn(Severity.ERROR);

        final RobotProblem problem2 = mock(RobotProblem.class);
        when(problem2.getMessage()).thenReturn("OtherProblem");
        when(problem2.getSeverity()).thenReturn(Severity.WARNING);

        final RobotProblem problem3 = mock(RobotProblem.class);
        when(problem3.getMessage()).thenReturn("MinorIssue");
        when(problem3.getSeverity()).thenReturn(Severity.INFO);

        final Map<IPath, Collection<RobotProblemWithPosition>> problems = new HashMap<>();
        problems.put(new Path("first_file"), newArrayList(
                    new RobotProblemWithPosition(problem1, new ProblemPosition(7)),
                    new RobotProblemWithPosition(problem2, new ProblemPosition(9))));
        problems.put(new Path("second_file"),
                newArrayList(new RobotProblemWithPosition(problem3, new ProblemPosition(1))));

        try (ReportWithCheckstyleFormat reporter = new ReportWithCheckstyleFormat(file)) {
            reporter.write(problems);
        }
        final String content = Files.contentOf(file, Charsets.UTF_8);

        assertThat(content).contains("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        assertThat(content).contains("<checkstyle");
        assertThat(content).contains("</checkstyle>");

        assertThat(content).contains("<file name=\"first_file\">");
        assertThat(content).contains("<file name=\"second_file\">");

        assertThat(content).contains("<error line=\"7\" message=\"ImportantProblem!!!\" severity=\"error\"/>");
        assertThat(content).contains("<error line=\"9\" message=\"OtherProblem\" severity=\"warning\"/>");
        assertThat(content).contains("<error line=\"1\" message=\"MinorIssue\" severity=\"info\"/>");
    }

}
