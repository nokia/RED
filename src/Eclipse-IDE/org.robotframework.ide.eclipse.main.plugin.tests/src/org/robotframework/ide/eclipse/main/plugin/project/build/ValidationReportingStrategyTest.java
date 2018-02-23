package org.robotframework.ide.eclipse.main.plugin.project.build;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.junit.Test;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;

public class ValidationReportingStrategyTest {

    private final ValidationReportingStrategy strategy = ValidationReportingStrategy.reportOnly();

    @Test
    public void ignoredProblemsShouldNotBeReported() {
        final RobotProblem problem = mock(RobotProblem.class);
        final IFile file = mock(IFile.class);
        final ProblemPosition position = new ProblemPosition(1);
        final Map<String, Object> additionalAttributes = new HashMap<>();

        when(problem.getSeverity()).thenReturn(Severity.IGNORE);

        strategy.handleProblem(problem, file, position, additionalAttributes);

        verify(problem, never()).createMarker(file, position, additionalAttributes);
    }
}
