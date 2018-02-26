package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotTask.Priority;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;

import com.google.common.collect.ImmutableMap;

public class RobotTasksReporterTest {


    @Test
    public void nothingIsReportedWhenThereIsNoTaskFound() {
        final Map<String, Priority> keywords = ImmutableMap.of("TASK", Priority.HIGH);
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.isTasksDetectionEnabled()).thenReturn(true);
        when(preferences.getTaskTagsWithPriorities()).thenReturn(keywords);
        
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  Log  # comment TODO this is a task")
                .build();
        
        final ValidationReportingStrategy markersReporter = mock(ValidationReportingStrategy.class);
        final RobotTasksReporter reporter = new RobotTasksReporter(model, markersReporter, preferences);
        
        reporter.reportTasks();
        
        verifyZeroInteractions(markersReporter);
    }

    @Test
    public void simpleTaskIsProperlyReported() {
        final Map<String, Priority> keywords = ImmutableMap.of("TASK", Priority.HIGH);
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.isTasksDetectionEnabled()).thenReturn(true);
        when(preferences.getTaskTagsWithPriorities()).thenReturn(keywords);
        
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  Log  # comment TASK this is a task")
                .build();
        
        final ValidationReportingStrategy markersReporter = mock(ValidationReportingStrategy.class);
        final RobotTasksReporter reporter = new RobotTasksReporter(model, markersReporter, preferences);
        
        reporter.reportTasks();

        verify(markersReporter).handleTask(eq(new RobotTask(Priority.HIGH, "TASK this is a task", 3)),
                nullable(IFile.class));
        verifyNoMoreInteractions(markersReporter);
    }

    @Test
    public void multipleTasksAreProperlyReported() {
        final Map<String, Priority> keywords = ImmutableMap.of("TASK", Priority.HIGH, "TODO", Priority.LOW);
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.isTasksDetectionEnabled()).thenReturn(true);
        when(preferences.getTaskTagsWithPriorities()).thenReturn(keywords);

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  Log  # comment TASK this is a task")
                .appendLine("  Log  # comment TODO this is a todo")
                .build();

        final ValidationReportingStrategy markersReporter = mock(ValidationReportingStrategy.class);
        final RobotTasksReporter reporter = new RobotTasksReporter(model, markersReporter, preferences);

        reporter.reportTasks();

        verify(markersReporter).handleTask(eq(new RobotTask(Priority.HIGH, "TASK this is a task", 3)),
                nullable(IFile.class));
        verify(markersReporter).handleTask(eq(new RobotTask(Priority.LOW, "TODO this is a todo", 4)),
                nullable(IFile.class));
        verifyNoMoreInteractions(markersReporter);
    }

    @Test
    public void mulitpleTasksAreProperlyReported_whenDefinedInSingleLine() {
        final Map<String, Priority> keywords = ImmutableMap.of("TASK", Priority.HIGH, "TODO", Priority.LOW);
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.isTasksDetectionEnabled()).thenReturn(true);
        when(preferences.getTaskTagsWithPriorities()).thenReturn(keywords);

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  Log  # comment TASK a task TODO todo")
                .build();

        final ValidationReportingStrategy markersReporter = mock(ValidationReportingStrategy.class);
        final RobotTasksReporter reporter = new RobotTasksReporter(model, markersReporter, preferences);

        reporter.reportTasks();

        verify(markersReporter).handleTask(eq(new RobotTask(Priority.HIGH, "TASK a task", 3)), nullable(IFile.class));
        verify(markersReporter).handleTask(eq(new RobotTask(Priority.LOW, "TODO todo", 3)), nullable(IFile.class));
        verifyNoMoreInteractions(markersReporter);
    }

    @Test
    public void taskDescriptionOnlyContainTextFromSameLine() {
        final Map<String, Priority> keywords = ImmutableMap.of("TASK", Priority.HIGH, "TODO", Priority.LOW);
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.isTasksDetectionEnabled()).thenReturn(true);
        when(preferences.getTaskTagsWithPriorities()).thenReturn(keywords);

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  Log  # comment TASK a task")
                .appendLine("  # which is defined in many lines")
                .build();

        final ValidationReportingStrategy markersReporter = mock(ValidationReportingStrategy.class);
        final RobotTasksReporter reporter = new RobotTasksReporter(model, markersReporter, preferences);

        reporter.reportTasks();

        verify(markersReporter).handleTask(eq(new RobotTask(Priority.HIGH, "TASK a task", 3)), nullable(IFile.class));
        verifyNoMoreInteractions(markersReporter);

    }

}
