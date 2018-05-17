/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.VersionAvailabilityInfo;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

/**
 * @author wypych
 */
public class HeaderDumperHelper {

    private final DumperHelper dumperHelper;

    public HeaderDumperHelper(final DumperHelper dumperHelper) {
        this.dumperHelper = dumperHelper;
    }

    private DumpLineUpdater getDumpLineUpdater() {
        return this.dumperHelper.getDumpLineUpdater();
    }

    public void dumpHeader(final RobotFile model, final TableHeader<? extends ARobotSectionTable> th,
            final List<RobotLine> lines) {
        if (!lines.isEmpty()) {
            final RobotLine lastLine = lines.get(lines.size() - 1);
            final IRobotLineElement endOfLine = lastLine.getEndOfLine();
            if ((endOfLine == null || endOfLine.getFilePosition().isNotSet()
                    || endOfLine.getTypes().contains(EndOfLineTypes.NON)
                    || endOfLine.getTypes().contains(EndOfLineTypes.EOF))
                    && !dumperHelper.getEmptyLineDumper().isEmptyLine(lastLine)) {
                final IRobotLineElement lineSeparator = dumperHelper.getLineSeparator(model);
                getDumpLineUpdater().updateLine(model, lines, lineSeparator);
            }
        }

        final RobotToken decToken = th.getDeclaration();
        final FilePosition filePosition = decToken.getFilePosition();
        int fileOffset = -1;
        if (filePosition != null && !filePosition.isNotSet()) {
            fileOffset = filePosition.getOffset();
        }

        RobotLine currentLine = null;
        if (fileOffset >= 0) {
            final Optional<Integer> lineIndex = model.getRobotLineIndexBy(fileOffset);
            if (lineIndex.isPresent()) {
                currentLine = model.getFileContent().get(lineIndex.get());
            }
        }

        boolean wasHeaderType = false;
        final RobotTokenType headerType = convertHeader(th.getModelType());
        final List<IRobotTokenType> tokenTypes = th.getDeclaration().getTypes();
        for (int index = 0; index < tokenTypes.size(); index++) {
            final IRobotTokenType tokenType = tokenTypes.get(index);
            if (RobotTokenType.isTableHeader(tokenType)) {
                if (headerType == tokenType) {
                    if (wasHeaderType) {
                        tokenTypes.remove(index);
                        index--;
                    }
                    wasHeaderType = true;
                } else {
                    tokenTypes.remove(index);
                    index--;
                }
            }
        }

        if (!wasHeaderType) {
            tokenTypes.clear();
            tokenTypes.add(headerType);
        }

        if ((decToken.getRaw() == null || decToken.getRaw().isEmpty())
                && (decToken.getText() == null || decToken.getText().isEmpty())) {
            final RobotVersion robotVersionInstalled = model.getParent().getRobotVersion();
            final VersionAvailabilityInfo vaiInCaseNoMatches = headerType
                    .getTheMostCorrectOneRepresentation(robotVersionInstalled);
            if (vaiInCaseNoMatches != null) {
                decToken.setText(vaiInCaseNoMatches.getRepresentation());
            }
        } else if (decToken.getText() == null || decToken.getText().isEmpty()) {
            decToken.setText(decToken.getRaw());
        }

        if (currentLine != null) {
            dumperHelper.getSeparatorDumpHelper().dumpSeparatorsBeforeToken(model, currentLine, decToken, lines);
        }

        getDumpLineUpdater().updateLine(model, lines, decToken);
        IRobotLineElement lastToken = decToken;

        List<IRobotLineElement> lineElements = new ArrayList<>(0);
        if (currentLine != null) {
            lineElements = currentLine.getLineElements();
            if (!decToken.isDirty()) {
                final int tokenPosIndex = lineElements.indexOf(decToken);
                if (lineElements.size() - 1 > tokenPosIndex + 1) {
                    final IRobotLineElement nextElem = lineElements.get(tokenPosIndex + 1);
                    if (nextElem.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                        getDumpLineUpdater().updateLine(model, lines, nextElem);
                        lastToken = nextElem;
                    }
                }
            }
        }

        for (final RobotToken columnToken : th.getColumnNames()) {
            if (currentLine != null) {
                dumperHelper.getSeparatorDumpHelper().dumpSeparatorsBeforeToken(model, currentLine, columnToken, lines);
            } else {
                getDumpLineUpdater().updateLine(model, lines,
                        dumperHelper.getSeparator(model, lines, lastToken, columnToken));
            }

            getDumpLineUpdater().updateLine(model, lines, columnToken);
            lastToken = columnToken;
            if (!columnToken.isDirty()) {
                final int thisTokenPosIndex = lineElements.indexOf(decToken);
                if (thisTokenPosIndex >= 0) {
                    if (lineElements.size() - 1 > thisTokenPosIndex + 1) {
                        final IRobotLineElement nextElem = lineElements.get(thisTokenPosIndex + 1);
                        if (nextElem.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                            getDumpLineUpdater().updateLine(model, lines, nextElem);
                            lastToken = nextElem;
                        }
                    }
                }
            }
        }

        for (final RobotToken commentPart : th.getComment()) {
            if (currentLine != null) {
                dumperHelper.getSeparatorDumpHelper().dumpSeparatorsBeforeToken(model, currentLine, commentPart, lines);
            } else {
                getDumpLineUpdater().updateLine(model, lines,
                        dumperHelper.getSeparator(model, lines, lastToken, commentPart));
            }

            getDumpLineUpdater().updateLine(model, lines, commentPart);
            lastToken = commentPart;
            if (!commentPart.isDirty()) {
                final int thisTokenPosIndex = lineElements.indexOf(decToken);
                if (thisTokenPosIndex >= 0) {
                    if (lineElements.size() - 1 > thisTokenPosIndex + 1) {
                        final IRobotLineElement nextElem = lineElements.get(thisTokenPosIndex + 1);
                        if (nextElem.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                            getDumpLineUpdater().updateLine(model, lines, nextElem);
                            lastToken = nextElem;
                        }
                    }
                }
            }
        }

        IRobotLineElement endOfLine = null;
        if (currentLine != null) {
            dumperHelper.getSeparatorDumpHelper().dumpSeparatorsAfterToken(model, currentLine, lastToken, lines);
            endOfLine = currentLine.getEndOfLine();
        }

        if (endOfLine != null) {
            final List<IRobotTokenType> types = endOfLine.getTypes();
            if (!types.contains(EndOfLineTypes.EOF) && !types.contains(EndOfLineTypes.NON)) {
                getDumpLineUpdater().updateLine(model, lines, endOfLine);
            }
        }

    }

    public RobotTokenType convertHeader(final ModelType modelType) {
        RobotTokenType type = RobotTokenType.UNKNOWN;
        if (modelType == ModelType.SETTINGS_TABLE_HEADER) {
            type = RobotTokenType.SETTINGS_TABLE_HEADER;
        } else if (modelType == ModelType.VARIABLES_TABLE_HEADER) {
            type = RobotTokenType.VARIABLES_TABLE_HEADER;
        } else if (modelType == ModelType.TEST_CASE_TABLE_HEADER) {
            type = RobotTokenType.TEST_CASES_TABLE_HEADER;
        } else if (modelType == ModelType.KEYWORDS_TABLE_HEADER) {
            type = RobotTokenType.KEYWORDS_TABLE_HEADER;
        }

        return type;
    }
}
