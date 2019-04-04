/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


public class RobotFormatterTest {

    @Test
    public void sameContentIsReturned_whenLineFormattersAreEmpty() throws Exception {
        final RobotFormatter formatter = new RobotFormatter("\n", true);
        final List<ILineFormatter> lineFormatters = spy(new ArrayList<>());

        final String formatted = formatter.format("abc", lineFormatters);

        assertThat(formatted).isEqualTo("abc");

        verify(lineFormatters).isEmpty();
        verifyNoMoreInteractions(lineFormatters);
    }

    @Test
    public void sameContentIsReturned_whenContentIsEmpty() throws Exception {
        final RobotFormatter formatter = new RobotFormatter("\n", true);

        final String formatted = formatter.format("", line -> line + " formatted");

        assertThat(formatted).isEqualTo("");
    }

    @Test
    public void formattedContentIsReturned_whenSingleLineFormatterIsProvided() throws Exception {
        final RobotFormatter formatter = new RobotFormatter("\n", false);

        final String formatted = formatter.format("abc\ndef\nghi\n", line -> "1 " + line + " 2");

        assertThat(formatted).isEqualTo("1 abc 2\n1 def 2\n1 ghi 2\n");
    }

    @Test
    public void formattedContentIsReturned_whenSeveralLineFormattersAreProvided() throws Exception {
        final RobotFormatter formatter = new RobotFormatter("\n", false);
        final List<ILineFormatter> lineFormatters = new ArrayList<>();
        lineFormatters.add(line -> "prefix " + line);
        lineFormatters.add(line -> "1 " + line + " 2");
        lineFormatters.add(line -> line + "$$$");

        final String formatted = formatter.format("abc\ndef\nghi\n", lineFormatters);

        assertThat(formatted).isEqualTo("1 prefix abc 2$$$\n1 prefix def 2$$$\n1 prefix ghi 2$$$\n");
    }

    @Test
    public void formattedContentIsReturned_whenEndsWithLineDelimiterAndSkippingLastLineDelimiterIsEnabled()
            throws Exception {
        final RobotFormatter formatter = new RobotFormatter("\n", true);

        final String formatted = formatter.format("abc\ndef\nghi\n", line -> line + line);

        assertThat(formatted).isEqualTo("abcabc\ndefdef\nghighi");
    }

    @Test
    public void formattedContentIsReturned_whenDoesNotEndWithLineDelimiterAndSkippingLastLineDelimiterIsEnabled()
            throws Exception {
        final RobotFormatter formatter = new RobotFormatter("\n", true);

        final String formatted = formatter.format("abc\ndef\nghi", line -> line + line);

        assertThat(formatted).isEqualTo("abcabc\ndefdef\nghighi");
    }

    @Test
    public void formattedContentIsReturned_whenEndsWithLineDelimiterAndSkippingLastLineDelimiterIsDisabled()
            throws Exception {
        final RobotFormatter formatter = new RobotFormatter("\n", false);

        final String formatted = formatter.format("abc\ndef\nghi\n", line -> line + line);

        assertThat(formatted).isEqualTo("abcabc\ndefdef\nghighi\n");
    }

    @Test
    public void formattedContentIsReturned_whenDoesNotEndWithLineDelimiterAndSkippingLastLineDelimiterIsDisabled()
            throws Exception {
        final RobotFormatter formatter = new RobotFormatter("\n", false);

        final String formatted = formatter.format("abc\ndef\nghi", line -> line + line);

        assertThat(formatted).isEqualTo("abcabc\ndefdef\nghighi\n");
    }

}
