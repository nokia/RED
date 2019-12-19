/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;

public class LineReader extends Reader {

    private final Reader reader;

    private int positionInFile = 0;

    private final Map<Integer, Constant> eols = new LinkedHashMap<>();

    public LineReader(final Reader reader) {
        this.reader = reader;
    }

    public List<Constant> getLineEnd(final int offset) {
        final List<Constant> endOfLine = new ArrayList<>();
        final Constant c1 = eols.get(offset);
        final Constant c2 = eols.get(offset + 1);

        if (c1 != null) {
            endOfLine.add(c1);
        }
        if (c2 != null && c2 != c1) {
            endOfLine.add(c2);
        }
        return endOfLine;
    }

    public List<FileRegion> getLinesRegion() {
        final List<FileRegion> eols = new ArrayList<>();

        int column = 0;
        int line = 0;
        int offset = 0;
        boolean skipNext = false;

        for (final Integer currentOffset : this.eols.keySet()) {
            if (skipNext) {
                skipNext = false;
                continue;
            }

            line++;
            column = 0;
            final List<Constant> eol = getLineEnd(currentOffset);
            int eolLength = eol.size();
            if (eolLength > 1) {
                skipNext = true;

            } else if (eol.get(0) == Constant.EOF) {
                eolLength = 0;
            }

            final int textLength = currentOffset - offset + eolLength;
            final FilePosition fpStart = new FilePosition(line, column, offset);
            offset = currentOffset + eolLength;
            final FilePosition fpEnd = new FilePosition(line, column + textLength, offset);

            eols.add(new FileRegion(fpStart, fpEnd));
        }
        return eols;
    }

    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        final int read = reader.read(cbuf, off, len);
        if (read <= 0) {
            eols.put(positionInFile, Constant.EOF);
            return read;
        }
        for (int i = 0; i < read; i++) {
            if (cbuf[i] == '\r') {
                eols.put(positionInFile + i, Constant.CR);
            } else if (cbuf[i] == '\n') {
                eols.put(positionInFile + i, Constant.LF);
            }
        }
        positionInFile += read;
        return read;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    public enum Constant {
        CR('\r'), LF('\n'), EOF((char) -1);

        private final char c;

        private Constant(final char c) {
            this.c = c;
        }

        public char getChar() {
            return c;
        }

        public static List<Constant> get(final IRobotLineElement rle) {
            return get(rle.getText());
        }

        public static List<Constant> get(final String lineEnding) {
            final char[] chars = lineEnding.toCharArray();

            if (chars.length == 0) {
                return newArrayList(EOF);
            }
            final List<Constant> converted = new ArrayList<>(0);
            for (final char c : chars) {
                converted.add(Constant.get(c));
            }
            return converted;
        }

        private static Constant get(final char c) {
            final Constant[] values = Constant.values();
            for (final Constant constant : values) {
                if (constant.getChar() == c) {
                    return constant;
                }
            }
            return null;
        }

        public static int getEndOfLineLength(final List<Constant> eols) {
            int size = 0;
            for (final Constant c : eols) {
                if (c != Constant.EOF) {
                    size++;
                } else {
                    break;
                }
            }
            return size;
        }
    }
}
