/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import java.util.List;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.LineReader.Constant;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;

/**
 * @author wypych
 */
public class DumpLineUpdater {

    private final DumperHelper aDumperHelper;

    public DumpLineUpdater(final DumperHelper aDumperHelper) {
        this.aDumperHelper = aDumperHelper;
    }

    public void updateLine(final RobotFile model, final List<RobotLine> outLines, final IRobotLineElement elem) {
        if (aDumperHelper.isEndOfLine(elem)) {
            if (outLines.isEmpty()) {
                final RobotLine line = new RobotLine(1, model);
                line.setEndOfLine(Constant.get(elem), 0, 0);
                outLines.add(line);
            } else {
                final RobotLine line = outLines.get(outLines.size() - 1);
                final FilePosition pos = getPosition(line, outLines);
                line.setEndOfLine(Constant.get(elem), pos.getOffset(), pos.getColumn());
            }

            if (!elem.getTypes().contains(EndOfLineTypes.EOF)) {
                outLines.add(new RobotLine(outLines.size() + 1, model));
            }
        } else {
            replaceEOFbyEOL(model, outLines);

            final RobotLine line;
            if (outLines.isEmpty()) {
                line = new RobotLine(1, model);
                outLines.add(line);
            } else {
                final int outLineSize = outLines.size();
                final RobotLine prevLine = outLines.get(outLineSize - 1);
                if (!prevLine.getEndOfLine().getTypes().contains(EndOfLineTypes.NON)) {
                    outLines.add(new RobotLine(prevLine.getLineNumber() + 1, model));
                }
                line = outLines.get(outLines.size() - 1);
            }

            final IRobotLineElement artToken = cloneWithPositionRecalculate(elem, line, outLines);
            if (elem instanceof Separator) {
                if (line.getLineElements().isEmpty() && artToken.getTypes().contains(SeparatorType.PIPE)) {
                    final Separator elemSep = (Separator) artToken;
                    final int pipeIndex = elemSep.getRaw().indexOf('|');
                    if (pipeIndex >= 1 && !(pipeIndex == 1 && elemSep.getRaw().charAt(0) == ' ')) {
                        elemSep.setRaw(elemSep.getRaw().substring(pipeIndex));
                        elemSep.setText(elemSep.getRaw());
                    }
                }
            }

            boolean isRobotToken = false;
            if (elem instanceof RobotToken) {
                isRobotToken = true;
                if (artToken.isDirty()) {
                    if (artToken.getRaw().isEmpty()) {
                        if (artToken instanceof RobotToken) {
                            final RobotToken rt = (RobotToken) artToken;
                            rt.setText(aDumperHelper.getEmpty());
                        }
                    } else {
                        if (artToken instanceof RobotToken) {
                            final RobotToken rt = (RobotToken) artToken;
                            final String text = formatWhiteSpace(rt.getText());
                            rt.setText(text);
                        }
                    }
                } else {
                    if (artToken.getText().isEmpty() && artToken.getRaw().isEmpty()
                            && (elem.isDirty() || elem.getFilePosition().isNotSet())) {
                        if (artToken instanceof RobotToken) {
                            if (isTokenToEmptyEscape((RobotToken) artToken)) {
                                final RobotToken rt = (RobotToken) artToken;
                                rt.setText(aDumperHelper.getEmpty());
                            }
                        }
                    } else if (elem.isDirty()) {
                        if (artToken instanceof RobotToken) {
                            final RobotToken rt = (RobotToken) artToken;
                            if (rt.getText().isEmpty() && isTokenToEmptyEscape(rt)) {
                                ((RobotToken) artToken).setText(aDumperHelper.getEmpty());
                            }
                        }
                    }
                }
            }

            final IRobotLineElement recalculated = cloneWithPositionRecalculate(artToken, line, outLines);
            if (isRobotToken) {
                aDumperHelper.notifyTokenDumpListener((RobotToken) elem, (RobotToken) recalculated);
            }
            line.addLineElement(recalculated);
        }
    }

    private void replaceEOFbyEOL(final RobotFile model, final List<RobotLine> outLines) {
        if (!aDumperHelper.isCurrentFileDirty() || outLines.isEmpty()) {
            return;
        }

        final int toCheck;
        if (outLines.get(outLines.size() - 1).getLineElements().isEmpty() && outLines.size() > 1) {
            toCheck = outLines.size() - 2;
        } else {
            toCheck = outLines.size() - 1;
        }

        final RobotLine previousLine = outLines.get(toCheck);
        final IRobotLineElement previousEol = previousLine.getEndOfLine();
        if (previousEol != null && previousEol.getTypes().contains(EndOfLineTypes.EOF)) {
            final IRobotLineElement lineSeparator = aDumperHelper.getLineSeparator(model);
            previousLine.setEndOfLine(Constant.get(lineSeparator), previousEol.getStartOffset(),
                    previousEol.getStartColumn());
            if (toCheck == outLines.size() - 1) {
                outLines.add(new RobotLine(previousLine.getLineNumber() + 1, model));
            }
        }
    }

    private boolean isTokenToEmptyEscape(final RobotToken token) {
        boolean result = false;
        final List<IRobotTokenType> types = token.getTypes();
        for (final IRobotTokenType tt : types) {
            if (tt == RobotTokenType.VARIABLES_VARIABLE_VALUE || RobotTokenType.getTypesForSettingsTable().contains(tt)
                    || (RobotTokenType.getTypesForTestCasesTable().contains(tt) && tt != RobotTokenType.TEST_CASE_NAME)
                    || (RobotTokenType.getTypesForKeywordsTable().contains(tt) && tt != RobotTokenType.KEYWORD_NAME)) {
                result = true;
                break;
            }
        }

        return result;
    }

    private FilePosition getPosition(final RobotLine line, final List<RobotLine> outLines) {
        return getPosition(line, outLines, 1);
    }

    private FilePosition getPosition(final RobotLine line, final List<RobotLine> outLines, final int last) {
        FilePosition pos = FilePosition.createNotSet();

        final IRobotLineElement endOfLine = line.getEndOfLine();
        if (endOfLine != null && !endOfLine.getFilePosition().isNotSet()) {
            pos = calculateEndPosition(endOfLine, true);
        } else if (!line.getLineElements().isEmpty()) {
            pos = calculateEndPosition(line.getLineElements().get(line.getLineElements().size() - 1), false);
        } else if (outLines != null && !outLines.isEmpty() && outLines.size() - last >= 0) {
            pos = getPosition(outLines.get(outLines.size() - last), outLines, last + 1);
        } else {
            pos = new FilePosition(1, 0, 0);
        }

        return pos;
    }

    private FilePosition calculateEndPosition(final IRobotLineElement elem, final boolean isEOL) {
        final FilePosition elemPos = elem.getFilePosition();

        final String raw = elem.getRaw();
        int rawLength = 0;
        if (raw != null) {
            rawLength = raw.length();
        }

        int textLength = 0;
        final String text = elem.getText();
        if (text != null) {
            textLength = text.length();
        }

        final int dataLength = Math.max(rawLength, textLength);

        return new FilePosition(elemPos.getLine(), isEOL ? 0 : elemPos.getColumn() + dataLength,
                elemPos.getOffset() + dataLength);
    }

    public IRobotLineElement cloneWithPositionRecalculate(final IRobotLineElement elem, final RobotLine line,
            final List<RobotLine> outLines) {
        IRobotLineElement newElem;
        if (elem instanceof RobotToken) {
            final RobotToken newToken = new RobotToken();
            newToken.setLineNumber(line.getLineNumber());
            newToken.setText(elem.getText());
            if (!elem.getTypes().isEmpty()) {
                newToken.getTypes().clear();
            }
            newToken.getTypes().addAll(elem.getTypes());
            final FilePosition pos = getPosition(line, outLines);
            newToken.setStartColumn(pos.getColumn());
            newToken.setStartOffset(pos.getOffset());

            newElem = newToken;
        } else {
            final Separator newSeparator = new Separator();
            newSeparator.setType((SeparatorType) elem.getTypes().get(0));
            newSeparator.setLineNumber(line.getLineNumber());
            if (elem.getRaw().isEmpty()) {
                newSeparator.setRaw(elem.getText());
            } else {
                newSeparator.setRaw(elem.getRaw());
            }
            newSeparator.setText(elem.getText());
            if (!elem.getTypes().isEmpty()) {
                newSeparator.getTypes().clear();
            }
            newSeparator.getTypes().addAll(elem.getTypes());
            final FilePosition pos = getPosition(line, outLines);
            newSeparator.setStartColumn(pos.getColumn());
            newSeparator.setStartOffset(pos.getOffset());

            newElem = newSeparator;
        }

        return newElem;
    }

    private String formatWhiteSpace(final String text) {
        String result = text;
        final StringBuilder str = new StringBuilder();
        char lastChar = (char) -1;
        if (text != null) {
            final char[] cArray = text.toCharArray();
            final int size = cArray.length;
            for (int cIndex = 0; cIndex < size; cIndex++) {
                final char c = cArray[cIndex];
                if (cIndex == 0) {
                    if (c == ' ') {
                        str.append("\\ ");
                    } else {
                        str.append(c);
                    }
                } else if (cIndex + 1 == size) {
                    if (c == ' ') {
                        str.append("\\ ");
                    } else {
                        str.append(c);
                    }
                } else {
                    if (lastChar == ' ' && c == ' ') {
                        str.append("\\ ");
                    } else {
                        str.append(c);
                    }
                }

                lastChar = c;
            }

            result = str.toString();
        }

        return result;
    }
}
