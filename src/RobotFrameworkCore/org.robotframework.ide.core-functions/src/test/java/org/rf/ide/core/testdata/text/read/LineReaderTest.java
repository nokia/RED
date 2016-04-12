/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;

public class LineReaderTest {

    @Test
    public void test_differentLineEnds() {
        // prepare
        StringReader textReader = new StringReader("test\rData\r\n");
        LineReader lineReader = new LineReader(textReader);
        flushAllDataToReader(lineReader);

        // execute
        final List<FileRegion> linesRegion = lineReader.getLinesRegion();

        // verify
        assertThat(linesRegion).hasSize(3);
        final FileRegion fileRegion = linesRegion.get(0);
        assertThat(fileRegion.getStart().isSamePlace(new FilePosition(1, 0, 0)));
        assertThat(fileRegion.getEnd().isSamePlace(new FilePosition(1, 5, 5)));
        final FileRegion fileRegion2 = linesRegion.get(1);
        assertThat(fileRegion2.getStart().isSamePlace(new FilePosition(2, 0, 5)));
        assertThat(fileRegion2.getEnd().isSamePlace(new FilePosition(2, 6, 11)));
        final FileRegion fileRegion3 = linesRegion.get(1);
        assertThat(fileRegion3.getStart().isSamePlace(new FilePosition(3, 0, 11)));
        assertThat(fileRegion3.getEnd().isSamePlace(new FilePosition(3, 0, 11)));
    }

    @Test
    public void test_emptyLine_andAfterLineAndEOF() {
        // prepare
        StringReader textReader = new StringReader("\r\nData");
        LineReader lineReader = new LineReader(textReader);
        flushAllDataToReader(lineReader);

        // execute
        final List<FileRegion> linesRegion = lineReader.getLinesRegion();

        // verify
        assertThat(linesRegion).hasSize(2);
        final FileRegion fileRegion = linesRegion.get(0);
        assertThat(fileRegion.getStart().isSamePlace(new FilePosition(1, 0, 0)));
        assertThat(fileRegion.getEnd().isSamePlace(new FilePosition(1, 2, 2)));
    }

    @Test
    public void test_emptyLine() {
        // prepare
        StringReader textReader = new StringReader("");
        LineReader lineReader = new LineReader(textReader);
        flushAllDataToReader(lineReader);

        // execute
        final List<FileRegion> linesRegion = lineReader.getLinesRegion();

        // verify
        assertThat(linesRegion).hasSize(1);
        final FileRegion fileRegion = linesRegion.get(0);
        assertThat(fileRegion.getStart().isSamePlace(new FilePosition(1, 0, 0)));
        assertThat(fileRegion.getEnd().isSamePlace(new FilePosition(1, 0, 0)));
    }

    private void flushAllDataToReader(final LineReader lineReader) {
        BufferedReader br = new BufferedReader(lineReader);
        try {
            @SuppressWarnings("unused")
            String line = null;
            while ((line = br.readLine()) != null) {
                line = null;
            }
        } catch (Exception e) {
            // is not I/O
        } finally {
            try {
                br.close();
            } catch (Exception e) {
                // will not happen
            }
        }
    }
}
