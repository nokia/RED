/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;

public class LineReader extends Reader {

    private final Reader reader;

    private int positionInFile = 0;

    private final Map<Integer, Constant> eOLs = new LinkedHashMap<>();

    public LineReader(final Reader reader) {
        this.reader = reader;
    }

    public List<Constant> getLineEnd(final int currentOffset) {
        final List<Constant> endOfLine = new ArrayList<>();
        final Constant c1 = eOLs.get(currentOffset);
        final Constant c2 = eOLs.get(currentOffset + 1);

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

        final Set<Integer> offsets = this.eOLs.keySet();
        for (final Integer currentOffset : offsets) {
            if (skipNext) {
                skipNext = false;
                continue;
            }

            line++;
            column = 0;
            final List<Constant> eol = getLineEnd(currentOffset);
            int eolSize = eol.size();
            if (eolSize > 1) {
                skipNext = true;
            } else {
                if (eol.get(0) == Constant.EOF) {
                    eolSize = 0;
                }
            }

            final int textLength = (currentOffset - offset) + eolSize;
            final FilePosition fpStart = new FilePosition(line, column, offset);
            offset = currentOffset + eolSize;
            final FilePosition fpEnd = new FilePosition(line, column + textLength, offset);

            eols.add(new FileRegion(fpStart, fpEnd));
        }

        return eols;
    }

    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        final int read = reader.read(cbuf, off, len);
        for (int i = 0; i < read; i++) {
            final Constant mapped = Constant.get(cbuf[i]);
            if (mapped != null) {
                int index = i;
                if (positionInFile > 0) {
                    index = positionInFile + index;
                }
                eOLs.put(index, mapped);
            }
        }
        if (read > 0) {
            positionInFile += read;
        } else {
            eOLs.put(positionInFile, Constant.EOF);
        }
        return read;
    }

    public int getPosition() {
        return positionInFile;
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

        public static Constant get(final char c) {
            Constant me = null;
            final Constant[] values = Constant.values();
            for (final Constant constant : values) {
                if (constant.getChar() == c) {
                    me = constant;
                    break;
                }
            }

            return me;
        }

        public static List<Constant> get(final IRobotLineElement rle) {
            final List<Constant> converted = new ArrayList<>(0);
            final char[] cArray = rle.getText().toCharArray();
            if (cArray.length > 0) {
                for (final char c : cArray) {
                    converted.add(Constant.get(c));
                }
            } else {
                converted.add(EOF);
            }

            return converted;
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
