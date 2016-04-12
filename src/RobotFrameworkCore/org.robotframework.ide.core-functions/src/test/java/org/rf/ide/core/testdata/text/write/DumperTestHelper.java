/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.text.read.LineReader;

public class DumperTestHelper {

    private static final DumperTestHelper INSTANCE = new DumperTestHelper();

    public static DumperTestHelper getINSTANCE() {
        return INSTANCE;
    }

    public Path getFile(final String path) throws URISyntaxException {
        URL resource = this.getClass().getResource(path);
        return Paths.get(resource.toURI());
    }

    public String readWithLineSeparatorPresave(final Path file) throws Exception {
        String text = null;

        InputStreamReader fisReader = null;
        try {
            fisReader = new InputStreamReader(new FileInputStream(file.toFile()));
            StringBuilder str = new StringBuilder();
            char buff[] = new char[4096];
            int length = -1;
            while ((length = fisReader.read(buff)) != -1) {
                str.append(Arrays.copyOf(buff, length));
            }

            text = str.toString();
        } finally {
            if (fisReader != null) {
                fisReader.close();
            }
        }

        return text;
    }

    public TextCompareResult compare(final String text1, final String text2) {
        TextCompareResult cmpResult = new TextCompareResult(text1, text2);

        int text1Len = text1.length();
        int text2Len = text2.length();
        int differenceOffset = FilePosition.NOT_SET;
        if (text1Len > text2Len) {
            differenceOffset = indexOfTheFirstDifference(text1.substring(0, text2Len), text2);
            if (differenceOffset == FilePosition.NOT_SET) {
                differenceOffset = text2Len;
            }
        } else if (text1Len < text2Len) {
            differenceOffset = indexOfTheFirstDifference(text1, text2.substring(0, text1Len));
            if (differenceOffset == FilePosition.NOT_SET) {
                differenceOffset = text1Len;
            }
        } else {
            differenceOffset = indexOfTheFirstDifference(text1, text2);
        }

        if (differenceOffset != FilePosition.NOT_SET) {
            cmpResult.setDifference(buildFrom(text1, differenceOffset), buildFrom(text2, differenceOffset));
        }

        return cmpResult;
    }

    private FilePosition buildFrom(final String text, final int offset) {
        FilePosition pos = FilePosition.createNotSet();

        FileRegion regionFound = null;
        final List<FileRegion> endOfLinesRegions = getEndOfLinesRegions(text);
        for (final FileRegion reg : endOfLinesRegions) {
            if (reg.isInside(offset)) {
                regionFound = reg;
                break;
            }
        }

        if (regionFound != null) {
            final FilePosition fpStart = regionFound.getStart();
            final int column = fpStart.getColumn() + (offset - fpStart.getOffset());
            pos = new FilePosition(fpStart.getLine(), column, offset);
        }

        if (pos.isNotSet()) {
            pos = new FilePosition(pos.getLine(), pos.getColumn(), offset);
        }

        return pos;
    }

    private int indexOfTheFirstDifference(final String text1, final String text2) {
        int differenceIndex = FilePosition.NOT_SET;

        final char text1CharArray[] = text1.toCharArray();
        final char text2CharArray[] = text2.toCharArray();
        int len = text1CharArray.length;
        for (int charIndex = 0; charIndex < len; charIndex++) {
            if (text1CharArray[charIndex] != text2CharArray[charIndex]) {
                differenceIndex = charIndex;
                break;
            }
        }

        return differenceIndex;
    }

    public static class TextCompareResult {

        private final String expected;

        private final String got;

        private FilePosition differenceStartExpectedText = FilePosition.createNotSet();

        private FilePosition differenceStartGotText = FilePosition.createNotSet();

        public TextCompareResult(final String expected, final String got) {
            this.expected = expected;
            this.got = got;
        }

        public String expected() {
            return this.expected;
        }

        public String got() {
            return this.got;
        }

        private void setDifference(final FilePosition differenceInExpected, final FilePosition differenceInGot) {
            this.differenceStartExpectedText = differenceInExpected;
            this.differenceStartGotText = differenceInGot;
        }

        public FilePosition getDifferenceInExpected() {
            return this.differenceStartExpectedText;
        }

        public FilePosition getDifferenceInGot() {
            return this.differenceStartGotText;
        }

        public String report() {
            String report = null;

            if (!getDifferenceInExpected().isNotSet() || !getDifferenceInGot().isNotSet()) {
                report = "Difference: \n" + "== Expected: \n==== Text: "
                        + expected().substring(getDifferenceInExpected().getOffset()) + "\n==== Position: "
                        + getDifferenceInExpected() + "\n== Got: \n==== Text: "
                        + got().substring(getDifferenceInGot().getOffset()) + "\n==== Position: "
                        + getDifferenceInGot();
            }

            return report;
        }
    }

    private List<FileRegion> getEndOfLinesRegions(final String text) {
        List<FileRegion> eols = new ArrayList<>();
        final LineReader lineReader = new LineReader(new StringReader(text));
        final BufferedReader bufferedReader = new BufferedReader(lineReader);

        try {
            @SuppressWarnings("unused")
            String lineText = null;
            while ((lineText = bufferedReader.readLine()) != null) {
            }

            eols = lineReader.getLinesRegion();
        } catch (Exception e) {
            // is not I/O will not happen
        } finally {
            try {
                bufferedReader.close();
            } catch (final IOException e) {
                // is not I/O will not happen
            }
        }

        return eols;
    }
}
