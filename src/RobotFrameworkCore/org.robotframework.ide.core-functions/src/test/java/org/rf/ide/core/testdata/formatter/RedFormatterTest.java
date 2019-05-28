/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.formatter.RedFormatter.FormatterSettings;


public class RedFormatterTest {

    @Test
    public void sameContentIsReturned_whenLineFormattersAreEmpty() throws Exception {
        final RedFormatter formatter = new RedFormatter(createSettings("\n", true), new HashSet<>());
        final List<ILineFormatter> lineFormatters = spy(new ArrayList<>());

        final String formatted = formatter.format("abc", lineFormatters);

        assertThat(formatted).isEqualTo("abc");

        verify(lineFormatters).isEmpty();
        verifyNoMoreInteractions(lineFormatters);
    }

    @Test
    public void sameContentIsReturned_whenContentIsEmpty() throws Exception {
        final RedFormatter formatter = new RedFormatter(createSettings("\n", true), new HashSet<>());

        final String formatted = formatter.format("", (k, line) -> line + " formatted");

        assertThat(formatted).isEqualTo("");
    }

    @Test
    public void formattedContentIsReturned_whenSingleLineFormatterIsProvided() throws Exception {
        final RedFormatter formatter = new RedFormatter(createSettings("\n", false), new HashSet<>());

        final String formatted = formatter.format("abc\ndef\nghi\n", (k, line) -> "1 " + line + " 2");

        assertThat(formatted).isEqualTo("1 abc 2\n1 def 2\n1 ghi 2\n");
    }

    @Test
    public void formattedContentIsReturned_whenSeveralLineFormattersAreProvided() throws Exception {
        final RedFormatter formatter = new RedFormatter(createSettings("\n", false), new HashSet<>());
        final List<ILineFormatter> lineFormatters = new ArrayList<>();
        lineFormatters.add((k, line) -> "prefix " + line);
        lineFormatters.add((k, line) -> "1 " + line + " 2");
        lineFormatters.add((k, line) -> line + "$$$");

        final String formatted = formatter.format("abc\ndef\nghi\n", lineFormatters);

        assertThat(formatted).isEqualTo("1 prefix abc 2$$$\n1 prefix def 2$$$\n1 prefix ghi 2$$$\n");
    }

    @Test
    public void formattedContentIsReturned_whenEndsWithLineDelimiterAndSkippingLastLineDelimiterIsEnabled()
            throws Exception {
        final RedFormatter formatter = new RedFormatter(createSettings("\n", true), new HashSet<>());

        final String formatted = formatter.format("abc\ndef\nghi\n", (k, line) -> line + line);

        assertThat(formatted).isEqualTo("abcabc\ndefdef\nghighi");
    }

    @Test
    public void formattedContentIsReturned_whenDoesNotEndWithLineDelimiterAndSkippingLastLineDelimiterIsEnabled()
            throws Exception {
        final RedFormatter formatter = new RedFormatter(createSettings("\n", true), new HashSet<>());

        final String formatted = formatter.format("abc\ndef\nghi", (k, line) -> line + line);

        assertThat(formatted).isEqualTo("abcabc\ndefdef\nghighi");
    }

    @Test
    public void formattedContentIsReturned_whenEndsWithLineDelimiterAndSkippingLastLineDelimiterIsDisabled()
            throws Exception {
        final RedFormatter formatter = new RedFormatter(createSettings("\n", false), new HashSet<>());

        final String formatted = formatter.format("abc\ndef\nghi\n", (k, line) -> line + line);

        assertThat(formatted).isEqualTo("abcabc\ndefdef\nghighi\n");
    }

    @Test
    public void formattedContentIsReturned_whenDoesNotEndWithLineDelimiterAndSkippingLastLineDelimiterIsDisabled()
            throws Exception {
        final RedFormatter formatter = new RedFormatter(createSettings("\n", false), new HashSet<>());

        final String formatted = formatter.format("abc\ndef\nghi", (k, line) -> line + line);

        assertThat(formatted).isEqualTo("abcabc\ndefdef\nghighi\n");
    }

    private static FormatterSettings createSettings(final String delimiter, final boolean skipDelimiterInLastLine) {
        final FormatterSettings settings = mock(FormatterSettings.class);
        when(settings.getLineDelimiter()).thenReturn(delimiter);
        when(settings.shouldSkipDelimiterInLastLine()).thenReturn(skipDelimiterInLastLine);
        return settings;
    }

}
