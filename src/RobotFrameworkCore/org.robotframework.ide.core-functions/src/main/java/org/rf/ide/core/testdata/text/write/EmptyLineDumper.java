/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Optional;

/**
 * @author wypych
 */
public class EmptyLineDumper {

    private DumperHelper dumperHelper;

    public EmptyLineDumper(final DumperHelper dumperHelper) {
        this.dumperHelper = dumperHelper;
    }

    public void dumpEmptyLines(final RobotFile model, final List<RobotLine> lines,
            final AModelElement<ARobotSectionTable> setting) {
        final FilePosition fPosEnd = setting.getEndPosition();
        if (!fPosEnd.isNotSet()) {
            if (!lines.isEmpty()) {
                RobotLine lastLine = lines.get(lines.size() - 1);
                IRobotLineElement endOfLine = lastLine.getEndOfLine();
                if ((endOfLine == null || endOfLine.getFilePosition().isNotSet()
                        || endOfLine.getTypes().contains(EndOfLineTypes.NON)
                        || endOfLine.getTypes().contains(EndOfLineTypes.EOF))
                        && !lastLine.getLineElements().isEmpty()) {
                    dumperHelper.getDumpLineUpdater().updateLine(model, lines,
                            dumperHelper.getLineSeparator(model, fPosEnd));
                }
            }

            Optional<Integer> currentLine = model.getRobotLineIndexBy(fPosEnd.getOffset());
            int currentLineNumber;
            if (currentLine.isPresent()) {
                currentLineNumber = currentLine.get();
            } else {
                currentLineNumber = fPosEnd.getLine();
            }

            final List<RobotLine> fileContent = model.getFileContent();
            while (fileContent.size() > currentLineNumber + 1) {
                final RobotLine nextLine = fileContent.get(currentLineNumber + 1);
                if (isEmptyLine(nextLine)) {
                    dumpLineDirectly(model, lines, nextLine);
                    currentLineNumber++;
                } else {
                    break;
                }
            }
        }
    }

    private void dumpLineDirectly(final RobotFile model, final List<RobotLine> outLines, final RobotLine currentLine) {
        for (final IRobotLineElement elem : currentLine.getLineElements()) {
            dumperHelper.getDumpLineUpdater().updateLine(model, outLines, elem);
        }

        final IRobotLineElement endOfLine = currentLine.getEndOfLine();
        if (endOfLine != null && !endOfLine.getFilePosition().isNotSet()) {
            dumperHelper.getDumpLineUpdater().updateLine(model, outLines, endOfLine);
        }
    }

    private boolean isEmptyLine(final RobotLine line) {
        boolean isEmpty = true;

        for (final IRobotLineElement elem : line.getLineElements()) {
            if (elem instanceof RobotToken) {
                RobotToken tok = (RobotToken) elem;
                if (!containsType(tok, RobotTokenType.PRETTY_ALIGN_SPACE)) {
                    isEmpty = false;
                    break;
                }
            }
        }

        return isEmpty;
    }

    private boolean containsType(final RobotToken token, final IRobotTokenType... types) {
        boolean contains = false;
        final List<IRobotTokenType> accepted = Arrays.asList(types);
        for (final IRobotTokenType type : token.getTypes()) {
            if (accepted.contains(type)) {
                contains = true;
            } else {
                contains = false;
                break;
            }
        }

        return contains;
    }
}
