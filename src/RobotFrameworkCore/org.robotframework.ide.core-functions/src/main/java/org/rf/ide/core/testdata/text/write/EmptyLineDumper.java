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

/**
 * @author wypych
 */
public class EmptyLineDumper {

    private DumperHelper dumperHelper;

    public EmptyLineDumper(final DumperHelper dumperHelper) {
        this.dumperHelper = dumperHelper;
    }

    public void dumpEmptyLines(final RobotFile model, final List<RobotLine> lines,
            final AModelElement<ARobotSectionTable> setting, boolean isLastElement) {
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

            int currentLineNumberInDump = lines.size() - 1;
            final List<RobotLine> fileContent = model.getFileContent();
            if (currentLineNumberInDump >= fileContent.size()) {
                return;
            }
            if (!isFirstLineEmpty(fileContent, currentLineNumberInDump)) {
                return;
            }
            while (fileContent.size() > currentLineNumberInDump) {
                final RobotLine nextLine = fileContent.get(currentLineNumberInDump);
                if (isEmptyLine(nextLine)) {
                    dumperHelper.dumpLineDirectly(model, lines, nextLine);
                    currentLineNumberInDump++;
                } else {
                    break;
                }
            }
        }
    }

    private boolean isFirstLineEmpty(final List<RobotLine> fileContent, final int currentLineNumberInDump) {
        return isEmptyLine(fileContent.get(currentLineNumberInDump));
    }

    public boolean isEmptyLine(final RobotLine line) {
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
