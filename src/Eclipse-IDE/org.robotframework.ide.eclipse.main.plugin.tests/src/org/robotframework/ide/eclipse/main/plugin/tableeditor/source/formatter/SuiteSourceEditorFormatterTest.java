/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentRewriteSessionListener;
import org.eclipse.jface.text.Region;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.testdata.formatter.ILineFormatter;
import org.rf.ide.core.testdata.formatter.RobotFormatter;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter.SuiteSourceEditorFormatter.FormattingSeparatorType;
import org.robotframework.red.junit.PreferenceUpdater;

public class SuiteSourceEditorFormatterTest {

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

    @Test
    public void documentIsNotChanged_whenFormatterPreferencesAreDisabled() throws Exception {
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, false);
        preferenceUpdater.setValue(RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, false);

        final SuiteSourceEditorFormatter formatter = new SuiteSourceEditorFormatter();
        final Document document = new Document("first ", "second   ", "  third  line  ", "  other  line   ");
        final Document documentSpy = spy(document);

        formatter.format(documentSpy, new Region(0, documentSpy.getLength()));

        assertThat(document).isEqualTo(new Document("first ", "second   ", "  third  line  ", "  other  line   "));

        verify(documentSpy, never()).replace(anyInt(), anyInt(), anyString());
    }

    @Test
    public void documentIsNotChanged_whenFormatterPreferencesAreEnabledButContentIsNotChanged() throws Exception {
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, true);
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_TYPE, FormattingSeparatorType.CONSTANT.name());
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_LENGTH, 2);
        preferenceUpdater.setValue(RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, true);

        final SuiteSourceEditorFormatter formatter = new SuiteSourceEditorFormatter();
        final Document document = new Document("first  line", "second  line", "", "");
        final Document documentSpy = spy(document);

        formatter.format(documentSpy, new Region(0, documentSpy.getLength()));

        assertThat(document).isEqualTo(new Document("first  line", "second  line", "", ""));

        verify(documentSpy, never()).replace(anyInt(), anyInt(), anyString());
    }

    @Test
    public void givenRegionIsFormatted() throws Exception {
        preferenceUpdater.setValue(RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, true);

        final SuiteSourceEditorFormatter formatter = new SuiteSourceEditorFormatter();
        final Document document = new Document("first ", "second   ", "  third  line  ", "  other  line   ");

        formatter.format(document, new Region(7, document.getLength() - 7));

        assertThat(document).isEqualTo(new Document("first ", "second", "  third  line", "  other  line"));
    }

    @Test
    public void givenLinesAreFormatted() throws Exception {
        preferenceUpdater.setValue(RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, true);

        final SuiteSourceEditorFormatter formatter = new SuiteSourceEditorFormatter();
        final Document document = new ExtendedDocument("first ", "second   ", "  third  line  ", "  other  line   ");

        formatter.format(document, newArrayList(0, 3));

        assertThat(document).isEqualTo(new ExtendedDocument("first", "second   ", "  third  line  ", "  other  line"));
    }

    @Test
    public void givenLinesAreFormatted_whenDocumentContainsSingleLineWithoutDelimiter() throws Exception {
        preferenceUpdater.setValue(RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, true);

        final SuiteSourceEditorFormatter formatter = new SuiteSourceEditorFormatter();
        final Document document = new ExtendedDocument("last   ");

        formatter.format(document, newArrayList(0));

        assertThat(document).isEqualTo(new ExtendedDocument("last"));
    }

    @Test
    public void givenLinesAreFormatted_whenRewriteSessionIsNotStarted() throws Exception {
        preferenceUpdater.setValue(RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, true);

        final SuiteSourceEditorFormatter formatter = new SuiteSourceEditorFormatter();
        final Document document = new Document("first ", "second   ", "  third ");

        formatter.format(document, newArrayList(0, 1));

        assertThat(document).isEqualTo(new Document("first", "second", "  third "));
    }

    @Test
    public void linesAreRightTrimmed() throws Exception {
        preferenceUpdater.setValue(RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, true);

        final SuiteSourceEditorFormatter formatter = new SuiteSourceEditorFormatter();
        final RobotFormatter robotFormatter = new RobotFormatter("\n", false);

        final String formatted = formatter.format("case \n  \n  Keyword  123 \t \n   [Return]   456   \n",
                robotFormatter);

        assertThat(formatted).isEqualTo("case\n\n  Keyword  123\n   [Return]   456\n");
    }

    @Test
    public void separatorLengthsAreAdjusted_whenConstantSeparatorTypeIsEnabled() throws Exception {
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, true);
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_TYPE, FormattingSeparatorType.CONSTANT.name());
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_LENGTH, 4);

        final SuiteSourceEditorFormatter formatter = new SuiteSourceEditorFormatter();
        final RobotFormatter robotFormatter = new RobotFormatter("\n", false);

        final String formatted = formatter.format("  Keyword      123  \n   OtherKeyword   456\n", robotFormatter);

        assertThat(formatted).isEqualTo("    Keyword    123    \n    OtherKeyword    456\n");
    }

    @Test
    public void separatorLengthsAreAdjusted_whenDynamicSeparatorTypeIsEnabled() throws Exception {
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, true);
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_TYPE, FormattingSeparatorType.DYNAMIC.name());
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_LENGTH, 4);

        final SuiteSourceEditorFormatter formatter = new SuiteSourceEditorFormatter();
        final RobotFormatter robotFormatter = new RobotFormatter("\n", false);

        final String formatted = formatter.format("  Keyword      123  \n   OtherKeyword   456\n", robotFormatter);

        assertThat(formatted).isEqualTo("    Keyword         123        \n    OtherKeyword    456    \n");
    }

    @Test
    public void allTabsAreReplacedWithSpaces_whenSeparatorAdjustmentIsEnabled() throws Exception {
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, true);
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_TYPE, FormattingSeparatorType.CONSTANT.name());
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_LENGTH, 2);

        final SuiteSourceEditorFormatter formatter = new SuiteSourceEditorFormatter();
        final RobotFormatter robotFormatter = new RobotFormatter("\n", true);

        final String formatted = formatter.format("\tKeyword\t 123\t\n\tNext \t \t line", robotFormatter);

        assertThat(formatted).isEqualTo("  Keyword  123  \n  Next  line");
    }

    @Test
    public void separatorLengthsAreAdjustedAndLinesAreRightTrimmed() throws Exception {
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, true);
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_TYPE, FormattingSeparatorType.CONSTANT.name());
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_LENGTH, 3);
        preferenceUpdater.setValue(RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, true);

        final SuiteSourceEditorFormatter formatter = new SuiteSourceEditorFormatter();
        final RobotFormatter robotFormatter = new RobotFormatter("\n", false);

        final String formatted = formatter.format("case \n  \n  Keyword    123 \t \n   [Return]    456 \t\t  \n",
                robotFormatter);

        assertThat(formatted).isEqualTo("case\n\n   Keyword   123\n   [Return]   456\n");
    }

    @Test
    public void contentIsNotChanged_whenIOExceptionIsThrownDuringFormatting() throws Exception {
        preferenceUpdater.setValue(RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, true);

        final SuiteSourceEditorFormatter formatter = new SuiteSourceEditorFormatter();
        final RobotFormatter robotFormatter = mock(RobotFormatter.class);
        when(robotFormatter.format(anyString(), any(ILineFormatter.class))).thenThrow(IOException.class);

        final String formatted = formatter.format("abc     def   ", robotFormatter);

        assertThat(formatted).isEqualTo("abc     def   ");
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
