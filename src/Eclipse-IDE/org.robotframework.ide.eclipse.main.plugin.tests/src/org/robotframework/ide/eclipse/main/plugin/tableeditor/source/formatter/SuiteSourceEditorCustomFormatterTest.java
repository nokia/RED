/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentRewriteSessionListener;
import org.eclipse.jface.text.Region;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.RobotParser;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RobotDocument;
import org.robotframework.red.junit.jupiter.BooleanPreference;
import org.robotframework.red.junit.jupiter.IntegerPreference;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.junit.jupiter.StringPreference;

@ExtendWith({ ProjectExtension.class, PreferencesExtension.class })
public class SuiteSourceEditorCustomFormatterTest {

    @Project
    static IProject project;

    @BooleanPreference(key = RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, value = false)
    @BooleanPreference(key = RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, value = false)
    @Test
    public void documentIsNotChanged_whenFormatterPreferencesAreDisabled() throws Exception {
        final SuiteSourceEditorCustomFormatter formatter = new SuiteSourceEditorCustomFormatter(
                RedPlugin.getDefault().getPreferences());
        final Document document = new Document("first ", "second   ", "  third  line  ", "  other  line   ");
        final Document documentSpy = spy(document);

        formatter.format(documentSpy, new Region(0, documentSpy.getLength()));

        assertThat(document).isEqualTo(new Document("first ", "second   ", "  third  line  ", "  other  line   "));

        verify(documentSpy, never()).replace(anyInt(), anyInt(), anyString());
    }

    @BooleanPreference(key = RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, value = true)
    @StringPreference(key = RedPreferences.FORMATTER_SEPARATOR_TYPE, value = "CONSTANT")
    @IntegerPreference(key = RedPreferences.FORMATTER_SEPARATOR_LENGTH, value = 2)
    @BooleanPreference(key = RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, value = true)
    @Test
    public void documentIsNotChanged_whenFormatterPreferencesAreEnabledButContentIsNotChanged() throws Exception {
        final SuiteSourceEditorCustomFormatter formatter = new SuiteSourceEditorCustomFormatter(
                RedPlugin.getDefault().getPreferences());
        final Document document = new Document("first  line", "second  line", "", "");
        final Document documentSpy = spy(document);

        formatter.format(documentSpy, new Region(0, documentSpy.getLength()));

        assertThat(document).isEqualTo(new Document("first  line", "second  line", "", ""));

        verify(documentSpy, never()).replace(anyInt(), anyInt(), anyString());
    }

    @BooleanPreference(key = RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, value = true)
    @Test
    public void givenRegionIsFormatted() throws Exception {
        final SuiteSourceEditorCustomFormatter formatter = new SuiteSourceEditorCustomFormatter(
                RedPlugin.getDefault().getPreferences());
        final Document document = new Document("first ", "second   ", "  third  line  ", "  other  line   ");

        formatter.format(document, new Region(7, document.getLength() - 7));

        assertThat(document).isEqualTo(new Document("first ", "second", "  third  line", "  other  line"));
    }

    @BooleanPreference(key = RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, value = true)
    @Test
    public void givenLinesAreFormatted() throws Exception {
        final SuiteSourceEditorCustomFormatter formatter = new SuiteSourceEditorCustomFormatter(
                RedPlugin.getDefault().getPreferences());
        final Document document = new ExtendedDocument("first ", "second   ", "  third  line  ", "  other  line   ");

        formatter.format(document, newArrayList(0, 3));

        assertThat(document).isEqualTo(new ExtendedDocument("first", "second   ", "  third  line  ", "  other  line"));
    }

    @BooleanPreference(key = RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, value = true)
    @Test
    public void givenLinesAreFormatted_whenDocumentContainsSingleLineWithoutDelimiter() throws Exception {
        final SuiteSourceEditorCustomFormatter formatter = new SuiteSourceEditorCustomFormatter(
                RedPlugin.getDefault().getPreferences());
        final Document document = new ExtendedDocument("last   ");

        formatter.format(document, newArrayList(0));

        assertThat(document).isEqualTo(new ExtendedDocument("last"));
    }

    @BooleanPreference(key = RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, value = true)
    @Test
    public void givenLinesAreFormatted_whenRewriteSessionIsNotStarted() throws Exception {
        final SuiteSourceEditorCustomFormatter formatter = new SuiteSourceEditorCustomFormatter(
                RedPlugin.getDefault().getPreferences());
        final Document document = new Document("first ", "second   ", "  third ");

        formatter.format(document, newArrayList(0, 1));

        assertThat(document).isEqualTo(new Document("first", "second", "  third "));
    }

    @BooleanPreference(key = RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, value = true)
    @StringPreference(key = RedPreferences.FORMATTER_SEPARATOR_TYPE, value = "DYNAMIC")
    @IntegerPreference(key = RedPreferences.FORMATTER_SEPARATOR_LENGTH, value = 4)
    @Test
    public void givenLinesAreFormattedTogether_whenTheyAreConsecutive() throws BadLocationException {
        final SuiteSourceEditorCustomFormatter formatter = new SuiteSourceEditorCustomFormatter(
                RedPlugin.getDefault().getPreferences());
        final Document document = new Document("a  b  c  d", "efghijk  lmnopqr", "s  t");

        formatter.format(document, newArrayList(1, 2));

        assertThat(document).isEqualTo(new Document("a  b  c  d", "efghijk    lmnopqr", "s          t"));
    }

    @BooleanPreference(key = RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, value = true)
    @Test
    public void linesAreRightTrimmed() throws Exception {
        final SuiteSourceEditorCustomFormatter formatter = new SuiteSourceEditorCustomFormatter(
                RedPlugin.getDefault().getPreferences());

        final Document document = new Document("case ", "  ", "  Keyword  123 \t ", "   [Return]   456   ", "");
        formatter.format(document);

        assertThat(document.get()).isEqualTo("case\n\n  Keyword  123\n   [Return]   456\n");
    }

    @BooleanPreference(key = RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, value = true)
    @StringPreference(key = RedPreferences.FORMATTER_SEPARATOR_TYPE, value = "CONSTANT")
    @IntegerPreference(key = RedPreferences.FORMATTER_SEPARATOR_LENGTH, value = 4)
    @Test
    public void separatorLengthsAreAdjusted_whenConstantSeparatorTypeIsEnabled() throws Exception {
        final SuiteSourceEditorCustomFormatter formatter = new SuiteSourceEditorCustomFormatter(
                RedPlugin.getDefault().getPreferences());

        final Document document = new Document("  Keyword      123  ", "   OtherKeyword   456", "");
        formatter.format(document);

        assertThat(document.get()).isEqualTo("    Keyword    123    \n    OtherKeyword    456\n");
    }

    @BooleanPreference(key = RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, value = true)
    @StringPreference(key = RedPreferences.FORMATTER_SEPARATOR_TYPE, value = "DYNAMIC")
    @IntegerPreference(key = RedPreferences.FORMATTER_SEPARATOR_LENGTH, value = 4)
    @Test
    public void separatorLengthsAreAdjusted_whenDynamicSeparatorTypeIsEnabled() throws Exception {
        final SuiteSourceEditorCustomFormatter formatter = new SuiteSourceEditorCustomFormatter(
                RedPlugin.getDefault().getPreferences());

        final Document document = new Document("  Keyword      123  ", "   OtherKeyword   456", "");
        formatter.format(document);

        assertThat(document.get()).isEqualTo("    Keyword         123    \n    OtherKeyword    456\n");
    }

    @BooleanPreference(key = RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, value = true)
    @StringPreference(key = RedPreferences.FORMATTER_SEPARATOR_TYPE, value = "CONSTANT")
    @IntegerPreference(key = RedPreferences.FORMATTER_SEPARATOR_LENGTH, value = 2)
    @Test
    public void allTabsAreReplacedWithSpaces_whenSeparatorAdjustmentIsEnabled() throws Exception {
        final SuiteSourceEditorCustomFormatter formatter = new SuiteSourceEditorCustomFormatter(
                RedPlugin.getDefault().getPreferences());

        final Document document = new Document("\tKeyword\t 123\t", "\tNext \t \t line");
        formatter.format(document);

        assertThat(document.get()).isEqualTo("  Keyword  123  \n  Next  line");
    }

    @BooleanPreference(key = RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, value = true)
    @StringPreference(key = RedPreferences.FORMATTER_SEPARATOR_TYPE, value = "CONSTANT")
    @IntegerPreference(key = RedPreferences.FORMATTER_SEPARATOR_LENGTH, value = 3)
    @BooleanPreference(key = RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, value = true)
    @Test
    public void separatorLengthsAreAdjustedAndLinesAreRightTrimmed() throws Exception {
        final SuiteSourceEditorCustomFormatter formatter = new SuiteSourceEditorCustomFormatter(
                RedPlugin.getDefault().getPreferences());

        final Document document = new Document("case ", "  ", "  Keyword    123 \t ", "   [Return]    456 \t\t  ", "");
        formatter.format(document);

        assertThat(document.get()).isEqualTo("case\n\n   Keyword   123\n   [Return]   456\n");
    }

    @BooleanPreference(key = RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, value = true)
    @StringPreference(key = RedPreferences.FORMATTER_SEPARATOR_TYPE, value = "CONSTANT")
    @IntegerPreference(key = RedPreferences.FORMATTER_SEPARATOR_LENGTH, value = 2)
    @Test
    public void newStyleFORLoopBodyIsIndented_whenInSameColumnAsForDeclaration() throws BadLocationException {
        final SuiteSourceEditorCustomFormatter formatter = new SuiteSourceEditorCustomFormatter(
                RedPlugin.getDefault().getPreferences());

        final RobotDocument document = robotDocument(
                "*** Test Cases ***",
                "case",
                "  FOR  ${x}  IN RANGE  10",
                "  Log  ${x}",
                "  kw  1  2  3",
                "  END",
                "  kw  1  2  3");
        formatter.format(document);

        assertThat(document.get()).isEqualTo(
                "*** Test Cases ***\ncase\n  FOR  ${x}  IN RANGE  10\n    Log  ${x}\n    kw  1  2  3\n  END\n  kw  1  2  3");
    }

    @BooleanPreference(key = RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, value = true)
    @StringPreference(key = RedPreferences.FORMATTER_SEPARATOR_TYPE, value = "CONSTANT")
    @IntegerPreference(key = RedPreferences.FORMATTER_SEPARATOR_LENGTH, value = 2)
    @Test
    public void newStyleFORLoopBodyIsNotIndented_whenInColumnGreaterThanForDeclaration() throws BadLocationException {
        final SuiteSourceEditorCustomFormatter formatter = new SuiteSourceEditorCustomFormatter(
                RedPlugin.getDefault().getPreferences());

        final RobotDocument document = robotDocument(
                "*** Test Cases ***",
                "case",
                "  FOR  ${x}  IN RANGE  10",
                "    Log  ${x}",
                "    kw  1  2  3",
                "  END",
                "  kw  1  2  3");
        formatter.format(document);

        assertThat(document.get()).isEqualTo(
                "*** Test Cases ***\ncase\n  FOR  ${x}  IN RANGE  10\n    Log  ${x}\n    kw  1  2  3\n  END\n  kw  1  2  3");
    }

    @BooleanPreference(key = RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, value = false)
    @Test
    public void newStyleFORLoopBodyIsNotIndented_whenInSameColumnAsForDeclarationAndSeparatorAdjustmentPreferenceIsNotEnabled()
            throws BadLocationException {
        final SuiteSourceEditorCustomFormatter formatter = new SuiteSourceEditorCustomFormatter(
                RedPlugin.getDefault().getPreferences());

        final RobotDocument document = robotDocument(
                "*** Test Cases ***",
                "case",
                "  FOR  ${x}  IN RANGE  10",
                "  Log  ${x}",
                "  kw  1  2  3",
                "  END",
                "  kw  1  2  3");
        formatter.format(document);

        assertThat(document.get()).isEqualTo(
                "*** Test Cases ***\ncase\n  FOR  ${x}  IN RANGE  10\n  Log  ${x}\n  kw  1  2  3\n  END\n  kw  1  2  3");
    }

    private static RobotDocument robotDocument(final String... lines) {
        final RobotProject robotProject = new RobotModel().createRobotProject(project);

        final RobotParser parser = new RobotParser(robotProject.getRobotProjectHolder(), new RobotVersion(3, 1));
        final File file = new File("file.robot");

        final RobotDocument document = new RobotDocument(parser, file);
        document.set(String.join("\n", lines));
        return document;
    }

    private static class ExtendedDocument extends Document implements IDocumentExtension4 {

        public ExtendedDocument(final String content) {
            super(content);
        }

        public ExtendedDocument(final String firstLine, final String... lines) {
            super(firstLine, lines);
        }

        @Override
        public DocumentRewriteSession startRewriteSession(final DocumentRewriteSessionType sessionType)
                throws IllegalStateException {
            return new DocumentRewriteSession(sessionType) {
                // nothing to do currently
            };
        }

        @Override
        public void stopRewriteSession(final DocumentRewriteSession session) {
            // nothing to do currently
        }

        @Override
        public DocumentRewriteSession getActiveRewriteSession() {
            return null;
        }

        @Override
        public void addDocumentRewriteSessionListener(final IDocumentRewriteSessionListener listener) {
            // nothing to do currently
        }

        @Override
        public void removeDocumentRewriteSessionListener(final IDocumentRewriteSessionListener listener) {
            // nothing to do currently
        }

        @Override
        public void replace(final int offset, final int length, final String text, final long modificationStamp)
                throws BadLocationException {
            // nothing to do currently
        }

        @Override
        public void set(final String text, final long modificationStamp) {
            // nothing to do currently
        }

        @Override
        public long getModificationStamp() {
            return 0;
        }

        @Override
        public String getDefaultLineDelimiter() {
            return null;
        }

        @Override
        public void setInitialLineDelimiter(final String lineDelimiter) {
            // nothing to do currently
        }

    }
}
