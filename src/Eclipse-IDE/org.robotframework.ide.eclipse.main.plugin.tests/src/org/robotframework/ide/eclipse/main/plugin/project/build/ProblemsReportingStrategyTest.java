package org.robotframework.ide.eclipse.main.plugin.project.build;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;

public class ProblemsReportingStrategyTest {

    private final ProblemsReportingStrategy strategy = ProblemsReportingStrategy.reportOnly();

    @Test
    public void ignoredProblemsShouldNotBeReported() {
        final RobotProblem problem = mock(RobotProblem.class);
        IFile file = mock(IFile.class);
        ProblemPosition position = new ProblemPosition(1);
        Map<String, Object> additionalAttributes = new HashMap<String, Object>();

        when(problem.getSeverity()).thenReturn(Severity.IGNORE);

        strategy.handleProblem(problem, file, position, additionalAttributes);

        verify(problem, never()).createMarker(file, position, additionalAttributes);
    }
}
