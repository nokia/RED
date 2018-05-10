/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsolePatternsListener.ExecutionArtifactsHyperlink;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsolePatternsListener.ExecutionWebsiteHyperlink;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

@RunWith(MockitoJUnitRunner.class)
public class RobotConsolePatternsListenerTest {

    private static final int DOCUMENT_OFFSET = 123;

    @Mock
    private IDocument document;

    @Mock
    private RobotProject robotProject;

    @Mock
    private TextConsole console;

    private RobotConsolePatternsListener listener;

    @Before
    public void beforeTest() {
        when(console.getDocument()).thenReturn(document);
        listener = new RobotConsolePatternsListener(robotProject);
        listener.connect(console);
    }

    @After
    public void afterTest() {
        listener.disconnect();
        listener = null;
    }

    @Test
    public void correctPatternIsReturned() throws Exception {
        assertThat(listener.getPattern()).isEqualTo("(Debug|Output|XUnit|Log|Report|Command):\\s*(.*)");
    }

    @Test
    public void correctLineQualifierIsReturned() throws Exception {
        assertThat(listener.getLineQualifier()).isEqualTo("(Debug|Output|XUnit|Log|Report|Command): ");
    }

    @Test
    public void correctCompilerFlagsAreReturned() throws Exception {
        assertThat(listener.getCompilerFlags()).isZero();
    }

    @Test
    public void exceptionIsThrown_whenListenerIsInInvalidState() throws Exception {
        final String matchedLine = "incorrect line";

        when(document.get(DOCUMENT_OFFSET, matchedLine.length())).thenReturn(matchedLine);
        assertThatIllegalStateException().isThrownBy(
                () -> listener.matchFound(new PatternMatchEvent(console, DOCUMENT_OFFSET, matchedLine.length())));

        verify(document).get(DOCUMENT_OFFSET, matchedLine.length());
        verifyNoMoreInteractions(document);

        verifyZeroInteractions(robotProject);

        verify(console).getDocument();
        verifyNoMoreInteractions(console);
    }

    @Test
    public void noHyperlinkIsAdded_whenPathIsEmpty() throws Exception {
        final String matchedLine = "Debug:   ";

        when(document.get(DOCUMENT_OFFSET, matchedLine.length())).thenReturn(matchedLine);
        listener.matchFound(new PatternMatchEvent(console, DOCUMENT_OFFSET, matchedLine.length()));

        verify(document).get(DOCUMENT_OFFSET, matchedLine.length());
        verifyNoMoreInteractions(document);

        verifyZeroInteractions(robotProject);

        verify(console).getDocument();
        verifyNoMoreInteractions(console);
    }

    @Test
    public void noHyperlinkIsAdded_whenRetrievingTextFromDocumentThrowsException() throws Exception {
        final String matchedLine = "Debug: debug_file.txt";

        when(document.get(DOCUMENT_OFFSET, matchedLine.length())).thenThrow(BadLocationException.class);
        listener.matchFound(new PatternMatchEvent(console, DOCUMENT_OFFSET, matchedLine.length()));

        verify(document).get(DOCUMENT_OFFSET, matchedLine.length());
        verifyNoMoreInteractions(document);

        verifyZeroInteractions(robotProject);

        verify(console).getDocument();
        verifyNoMoreInteractions(console);
    }

    @Test
    public void artifactsHyperlinkIsAdded_forDebugFile() throws Exception {
        final String matchedLine = "Debug: debug_file.txt";

        when(document.get(DOCUMENT_OFFSET, matchedLine.length())).thenReturn(matchedLine);
        listener.matchFound(new PatternMatchEvent(console, DOCUMENT_OFFSET, matchedLine.length()));

        verify(document).get(DOCUMENT_OFFSET, matchedLine.length());
        verifyNoMoreInteractions(document);

        verify(robotProject).getProject();
        verifyNoMoreInteractions(robotProject);

        verify(console).getDocument();
        verify(console).addHyperlink(isA(ExecutionArtifactsHyperlink.class), eq(DOCUMENT_OFFSET + "Debug: ".length()),
                eq("debug_file.txt".length()));
        verifyNoMoreInteractions(console);
    }

    @Test
    public void artifactsHyperlinkIsAdded_forOutputFile() throws Exception {
        final String matchedLine = "Output: output_file.xml";

        when(document.get(DOCUMENT_OFFSET, matchedLine.length())).thenReturn(matchedLine);
        listener.matchFound(new PatternMatchEvent(console, DOCUMENT_OFFSET, matchedLine.length()));

        verify(document).get(DOCUMENT_OFFSET, matchedLine.length());
        verifyNoMoreInteractions(document);

        verify(robotProject).getProject();
        verifyNoMoreInteractions(robotProject);

        verify(console).getDocument();
        verify(console).addHyperlink(isA(ExecutionArtifactsHyperlink.class), eq(DOCUMENT_OFFSET + "Output: ".length()),
                eq("output_file.xml".length()));
        verifyNoMoreInteractions(console);
    }

    @Test
    public void artifactsHyperlinkIsAdded_forXUnitFile() throws Exception {
        final String matchedLine = "XUnit: xunit_file.xml";

        when(document.get(DOCUMENT_OFFSET, matchedLine.length())).thenReturn(matchedLine);
        listener.matchFound(new PatternMatchEvent(console, DOCUMENT_OFFSET, matchedLine.length()));

        verify(document).get(DOCUMENT_OFFSET, matchedLine.length());
        verifyNoMoreInteractions(document);

        verify(robotProject).getProject();
        verifyNoMoreInteractions(robotProject);

        verify(console).getDocument();
        verify(console).addHyperlink(isA(ExecutionArtifactsHyperlink.class), eq(DOCUMENT_OFFSET + "XUnit: ".length()),
                eq("xunit_file.xml".length()));
        verifyNoMoreInteractions(console);
    }

    @Test
    public void artifactsHyperlinkIsAdded_forLogFile() throws Exception {
        final String matchedLine = "Log: log_file.html";

        when(document.get(DOCUMENT_OFFSET, matchedLine.length())).thenReturn(matchedLine);
        listener.matchFound(new PatternMatchEvent(console, DOCUMENT_OFFSET, matchedLine.length()));

        verify(document).get(DOCUMENT_OFFSET, matchedLine.length());
        verifyNoMoreInteractions(document);

        verify(robotProject).getProject();
        verifyNoMoreInteractions(robotProject);

        verify(console).getDocument();
        verify(console).addHyperlink(isA(ExecutionArtifactsHyperlink.class), eq(DOCUMENT_OFFSET + "Log: ".length()),
                eq("log_file.html".length()));
        verifyNoMoreInteractions(console);
    }

    @Test
    public void artifactsHyperlinkIsAdded_forReportFile() throws Exception {
        final String matchedLine = "Report: report_file.html";

        when(document.get(DOCUMENT_OFFSET, matchedLine.length())).thenReturn(matchedLine);
        listener.matchFound(new PatternMatchEvent(console, DOCUMENT_OFFSET, matchedLine.length()));

        verify(document).get(DOCUMENT_OFFSET, matchedLine.length());
        verifyNoMoreInteractions(document);

        verify(robotProject).getProject();
        verifyNoMoreInteractions(robotProject);

        verify(console).getDocument();
        verify(console).addHyperlink(isA(ExecutionArtifactsHyperlink.class), eq(DOCUMENT_OFFSET + "Report: ".length()),
                eq("report_file.html".length()));
        verifyNoMoreInteractions(console);
    }

    @Test
    public void artifactsHyperlinkIsAdded_whenCommandContainsArgumentFile() throws Exception {
        final String matchedLine = "Command: python.exe -m robot.run --argumentfile path_to_file.arg path_to_project";

        when(document.get(DOCUMENT_OFFSET, matchedLine.length())).thenReturn(matchedLine);
        listener.matchFound(new PatternMatchEvent(console, DOCUMENT_OFFSET, matchedLine.length()));

        verify(document).get(DOCUMENT_OFFSET, matchedLine.length());
        verifyNoMoreInteractions(document);

        verify(robotProject).getProject();
        verifyNoMoreInteractions(robotProject);

        verify(console).getDocument();
        verify(console).addHyperlink(isA(ExecutionArtifactsHyperlink.class),
                eq(DOCUMENT_OFFSET + "Command: python.exe -m robot.run --argumentfile ".length()),
                eq("path_to_file.arg".length()));
        verifyNoMoreInteractions(console);
    }

    @Test
    public void noHyperlinkIsAdded_whenCommandDoesNotContainArgumentFile() throws Exception {
        final String matchedLine = "Command: python.exe -m robot.run path_to_project";

        when(document.get(DOCUMENT_OFFSET, matchedLine.length())).thenReturn(matchedLine);
        listener.matchFound(new PatternMatchEvent(console, DOCUMENT_OFFSET, matchedLine.length()));

        verify(document).get(DOCUMENT_OFFSET, matchedLine.length());
        verifyNoMoreInteractions(document);

        verifyZeroInteractions(robotProject);

        verify(console).getDocument();
        verifyNoMoreInteractions(console);
    }

    @Test
    public void websiteHyperlinkIsAdded_whenPathIsHttpUri() throws Exception {
        final String matchedLine = "Output: http://1.2.3.4/folder/file.xml";

        when(document.get(DOCUMENT_OFFSET, matchedLine.length())).thenReturn(matchedLine);
        listener.matchFound(new PatternMatchEvent(console, DOCUMENT_OFFSET, matchedLine.length()));

        verify(document).get(DOCUMENT_OFFSET, matchedLine.length());
        verifyNoMoreInteractions(document);

        verifyZeroInteractions(robotProject);

        verify(console).getDocument();
        verify(console).addHyperlink(isA(ExecutionWebsiteHyperlink.class), eq(DOCUMENT_OFFSET + "Output: ".length()),
                eq("http://1.2.3.4/folder/file.xml".length()));
        verifyNoMoreInteractions(console);
    }

    @Test
    public void websiteHyperlinkIsAdded_whenPathIsHttpsUri() throws Exception {
        final String matchedLine = "Output: https://www.domain.com/folder/file.xml";

        when(document.get(DOCUMENT_OFFSET, matchedLine.length())).thenReturn(matchedLine);
        listener.matchFound(new PatternMatchEvent(console, DOCUMENT_OFFSET, matchedLine.length()));

        verify(document).get(DOCUMENT_OFFSET, matchedLine.length());
        verifyNoMoreInteractions(document);

        verifyZeroInteractions(robotProject);

        verify(console).getDocument();
        verify(console).addHyperlink(isA(ExecutionWebsiteHyperlink.class), eq(DOCUMENT_OFFSET + "Output: ".length()),
                eq("https://www.domain.com/folder/file.xml".length()));
        verifyNoMoreInteractions(console);
    }

    @Test
    public void noHyperlinkIsAdded_whenPathIsInvalidUri() throws Exception {
        final String matchedLine = "Output: https://invalid/file.xml?param 123";

        when(document.get(DOCUMENT_OFFSET, matchedLine.length())).thenReturn(matchedLine);
        listener.matchFound(new PatternMatchEvent(console, DOCUMENT_OFFSET, matchedLine.length()));

        verify(document).get(DOCUMENT_OFFSET, matchedLine.length());
        verifyNoMoreInteractions(document);

        verifyZeroInteractions(robotProject);

        verify(console).getDocument();
        verifyNoMoreInteractions(console);
    }

}
