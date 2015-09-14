/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class LineReader extends Reader {

    private final Reader reader;
    private int positionInFile = 0;
    private Map<Integer, Constant> eOLs = new LinkedHashMap<>();


    public LineReader(final Reader reader) {
        this.reader = reader;
    }


    public List<Constant> getLineEnd(int currentOffset) {
        List<Constant> endOfLine = new LinkedList<>();
        Constant c1 = eOLs.get(currentOffset);
        Constant c2 = eOLs.get(currentOffset + 1);

        if (c1 != null) {
            endOfLine.add(c1);
        }

        if (c2 != null && c2 != c1) {
            endOfLine.add(c2);
        }

        return endOfLine;
    }


    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int read = reader.read(cbuf, off, len);
        for (int i = 0; i < read; i++) {
            Constant mapped = Constant.get(cbuf[i]);
            if (mapped != null) {
                eOLs.put(i, mapped);
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


        private Constant(char c) {
            this.c = c;
        }


        public char getChar() {
            return c;
        }


        public static Constant get(char c) {
            Constant me = null;
            Constant[] values = Constant.values();
            for (Constant constant : values) {
                if (constant.getChar() == c) {
                    me = constant;
                    break;
                }
            }

            return me;
        }
    }
}
