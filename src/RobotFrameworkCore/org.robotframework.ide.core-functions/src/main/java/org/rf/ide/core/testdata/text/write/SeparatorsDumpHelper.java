/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import java.util.List;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Optional;

/**
 * @author wypych
 */
public class SeparatorsDumpHelper {

    private final DumperHelper dumpHelper;

    public SeparatorsDumpHelper(final DumperHelper dumpHelper) {
        this.dumpHelper = dumpHelper;
    }

    public void dumpSeparatorsAfterToken(final RobotFile model, final RobotLine currentLine,
            final IRobotLineElement currentToken, final List<RobotLine> lines) {
        int dumpEndIndex = -1;
        List<IRobotLineElement> lineElements = currentLine.getLineElements();
        currentLine.getEndOfLine();
        Optional<Integer> forCurrentLine = getDumpEndIndex(lineElements, currentToken);
        if (forCurrentLine.isPresent()) {
            dumpEndIndex = forCurrentLine.get();
        } else {
            if (currentToken.getLineNumber() != FilePosition.NOT_SET) {
                final RobotLine robotLine = model.getFileContent().get(currentToken.getLineNumber() - 1);
                lineElements = robotLine.getLineElements();
                Optional<Integer> forTokenLine = getDumpEndIndex(lineElements, currentToken);
                if (forTokenLine.isPresent()) {
                    dumpEndIndex = forTokenLine.get();
                }
            }
        }

        if (dumpEndIndex >= 0) {
            int tokenPosIndex = lineElements.indexOf(currentToken);
            for (int myIndex = tokenPosIndex + 1; myIndex < lineElements.size() && myIndex <= dumpEndIndex; myIndex++) {
                dumpHelper.getDumpLineUpdater().updateLine(model, lines, lineElements.get(myIndex));
            }
        }

    }

    private Optional<Integer> getDumpEndIndex(final List<IRobotLineElement> lineElements,
            final IRobotLineElement currentToken) {
        Optional<Integer> dumpEndIndex = Optional.absent();

        int tokenPosIndex = lineElements.indexOf(currentToken);
        if (tokenPosIndex > -1) {
            for (int index = tokenPosIndex + 1; index < lineElements.size(); index++) {
                if (lineElements.get(index) instanceof RobotToken) {
                    break;
                } else {
                    dumpEndIndex = Optional.of(index);
                }
            }
        }

        return dumpEndIndex;
    }

    public void dumpSeparatorsBeforeToken(final RobotFile model, final RobotLine currentLine,
            final IRobotLineElement currentToken, final List<RobotLine> lines) {
        int dumpStartIndex = -1;
        final List<IRobotLineElement> lineElements = currentLine.getLineElements();
        final int tokenPosIndex = lineElements.indexOf(currentToken);
        for (int index = tokenPosIndex - 1; index >= 0; index--) {
            IRobotLineElement lineElem = lineElements.get(index);
            if (lineElem instanceof RobotToken) {
                if (!lineElem.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                    break;
                } else {
                    dumpStartIndex = index;
                }
            } else {
                dumpStartIndex = index;
            }
        }

        if (dumpStartIndex >= 0) {
            for (int myIndex = dumpStartIndex; myIndex < tokenPosIndex; myIndex++) {
                dumpHelper.getDumpLineUpdater().updateLine(model, lines, lineElements.get(myIndex));
            }
        }
    }
}
