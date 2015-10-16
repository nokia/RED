/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.columnSeparators;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public abstract class ALineSeparator {

    private AtomicBoolean wasInitialized = new AtomicBoolean(false);
    private int prevElementIndex = -1;
    private int elementIndex = -1;
    protected final int lineNumber;
    protected final String line;
    private final List<IRobotLineElement> lineElements = new LinkedList<>();


    protected ALineSeparator(final int lineNumber, final String line) {
        this.lineNumber = lineNumber;
        this.line = line;
    }


    private void init() {
        synchronized (this) {
            if (!wasInitialized.get()) {
                wasInitialized.set(true);
                int lastColumnProcessed = 0;
                int textLength = line.length();

                if (textLength > 0) {
                    while(lastColumnProcessed < textLength) {
                        if (hasNextSeparator()) {
                            Separator currentSeparator = nextSeparator();
                            int startColumn = currentSeparator.getStartColumn();
                            int remainingData = startColumn
                                    - lastColumnProcessed;
                            String rawText = line.substring(
                                    lastColumnProcessed, startColumn);
                            if (remainingData > 0 || !lineElements.isEmpty()) {
                                RobotToken token = new RobotToken();
                                token.setLineNumber(lineNumber);
                                token.setRaw(new StringBuilder(rawText));
                                token.setText(new StringBuilder(rawText));
                                token.setStartColumn(lastColumnProcessed);
                                token.setType(RobotTokenType.UNKNOWN);

                                lineElements.add(token);
                            }

                            lineElements.add(currentSeparator);
                            lastColumnProcessed = currentSeparator
                                    .getEndColumn();
                        } else {
                            String rawText = line
                                    .substring(lastColumnProcessed);
                            RobotToken token = new RobotToken();
                            token.setLineNumber(lineNumber);
                            token.setRaw(new StringBuilder(rawText));
                            token.setText(new StringBuilder(rawText));
                            token.setStartColumn(lastColumnProcessed);
                            token.setType(RobotTokenType.UNKNOWN);

                            lineElements.add(token);

                            lastColumnProcessed = textLength;
                        }
                    }
                }
            }
        }
    }


    protected abstract Separator nextSeparator();


    public Separator next() {
        init();
        updateIndex();
        Separator found = null;
        if (elementIndex > prevElementIndex) {
            found = (Separator) lineElements.get(elementIndex);
        }

        prevElementIndex = elementIndex;

        return found;
    }


    protected abstract boolean hasNextSeparator();


    public boolean hasNext() {
        init();
        updateIndex();

        return (elementIndex > prevElementIndex);
    }


    private void updateIndex() {
        int elementsSize = lineElements.size();
        if (prevElementIndex < elementsSize && elementIndex <= prevElementIndex) {
            for (int index = elementIndex + 1; index < elementsSize; index++) {
                if (isSeparator(lineElements.get(index))) {
                    elementIndex = index;
                    break;
                }
            }
        }
    }


    private boolean isSeparator(final IRobotLineElement elem) {
        boolean result = false;
        List<IRobotTokenType> types = elem.getTypes();
        for (IRobotTokenType type : types) {
            if (type == SeparatorType.PIPE
                    || type == SeparatorType.TABULATOR_OR_DOUBLE_SPACE) {
                result = true;
                break;
            }
        }

        return result;
    }


    public abstract SeparatorType getProducedType();


    public List<IRobotLineElement> getSplittedLine() {
        return Collections.unmodifiableList(lineElements);
    }


    public int getPreviousElementIndex() {
        return prevElementIndex;
    }


    public int getCurrentElementIndex() {
        return elementIndex;
    }


    public int getLineNumber() {
        return lineNumber;
    }


    public String getLine() {
        return line;
    }
}
