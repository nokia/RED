/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.LineReader.Constant;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.RedTokenScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.RedTokensQueueBuilder;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;

public class ShellTokensScanner extends RedTokenScanner {

    public ShellTokensScanner(final ISyntaxColouringRule... rules) {
        super(rules);
    }

    @Override
    public IToken nextToken() {
        return nextToken(() -> {
            lines = getLines((ShellDocument) document);
            return new RedTokensQueueBuilder().buildQueue(rangeOffset, rangeLength, lines, rangeLine);
        });
    }

    private List<RobotLine> getLines(final ShellDocument shellDocument) {
        final RangeMap<Integer, String> positions = shellDocument.getPositionsRanges();

        final List<RobotLine> lines = new ArrayList<>();
        ExpressionType currentType = null;
        for (int i = 0; i < shellDocument.getNumberOfLines(); i++) {
            final int offset = shellDocument.getLineInfo(i).getOffset();

            final String line = shellDocument.getLine(i);
            if (ShellDocument.isModeCategory(positions.get(offset))) {
                currentType = getType(line);

            } else if (!ShellDocument.isModeContinuationCategory(positions.get(offset))) {
                currentType = null;
            }

            final RobotLine parsedLine = parseLine(line, i + 1, offset, currentType, positions);
            addLineEnding(parsedLine, shellDocument.getLineDelimiter(i), offset);
            lines.add(parsedLine);
        }
        return lines;
    }

    private RobotLine parseLine(final String line, final int lineNumber, final int offset,
            final ExpressionType currentType, final RangeMap<Integer, String> positions) {
        final RobotLine robotLine = new RobotLine(lineNumber, null);

        if (currentType != null) {
            final boolean isContinuation = ShellDocument.isModeContinuationCategory(positions.get(offset));
            parseExpressionLine(robotLine, line, offset, currentType, isContinuation);
        } else {
            parseResultLine(robotLine, line, offset, positions);
        }
        return robotLine;
    }

    private void parseExpressionLine(final RobotLine robotLine, final String line, final int offset,
            final ExpressionType currentType, final boolean isContinuation) {
        final Pattern pattern = Pattern.compile("  +");
        final int prefixSplitIndex = currentType.name().length() + 2;
        final String prefix = line.substring(0, prefixSplitIndex);
        final String expression = line.substring(prefixSplitIndex);

        final IRobotTokenType modeType = isContinuation ? ShellTokenType.MODE_CONTINUATION : ShellTokenType.MODE_FLAG;
        addToken(robotLine, RobotToken.create(prefix, modeType), offset);

        if (currentType == ExpressionType.ROBOT) {
            final Matcher matcher = pattern.matcher(expression);
            int i = 0;
            while (matcher.find()) {
                final int j = matcher.start();

                final boolean isFirst = robotLine.getLineElements().size() == 1;
                final IRobotTokenType mainType = isFirst && !isContinuation ? ShellTokenType.CALL_KW
                        : ShellTokenType.CALL_ARG;
                addToken(robotLine,
                        RobotToken.create(expression.substring(i, j), mainType, RobotTokenType.VARIABLE_USAGE), offset);

                i = matcher.end();
                addSeparator(robotLine, Separator.spacesSeparator(i - j), offset);
            }
            final boolean isFirst = robotLine.getLineElements().size() == 1;
            final IRobotTokenType mainType = isFirst && !isContinuation ? ShellTokenType.CALL_KW
                    : ShellTokenType.CALL_ARG;
            addToken(robotLine, RobotToken.create(expression.substring(i), mainType, RobotTokenType.VARIABLE_USAGE),
                    offset);

        } else {
            final IRobotTokenType tokenType = currentType == ExpressionType.VARIABLE ? RobotTokenType.VARIABLE_USAGE
                    : RobotTokenType.UNKNOWN;
            addToken(robotLine, RobotToken.create(expression, tokenType), offset);
        }
    }

    private void parseResultLine(final RobotLine robotLine, final String line, final int offset,
            final RangeMap<Integer, String> positions) {

        final RangeMap<Integer, String> thisLineRanges = positions
                .subRangeMap(Range.closedOpen(offset, offset + line.length()));
        final Map<Range<Integer>, String> thisLineRangesMap = thisLineRanges.asMapOfRanges();

        if (thisLineRangesMap.isEmpty()) {
            addToken(robotLine, RobotToken.create(line, RobotTokenType.UNKNOWN), offset);

        } else {
            int start = offset;
            for (final Range<Integer> range : thisLineRangesMap.keySet()) {
                final String partBeforeRange = line.substring(start - offset, range.lowerEndpoint() - offset);
                final String partInRange = line.substring(range.lowerEndpoint() - offset,
                        range.upperEndpoint() - offset);

                if (!partBeforeRange.isEmpty()) {
                    addToken(robotLine, RobotToken.create(partBeforeRange, RobotTokenType.UNKNOWN), start);
                }
                IRobotTokenType type;
                if (ShellDocument.isResultPassCategory(thisLineRangesMap.get(range))) {
                    type = ShellTokenType.PASS;
                } else if (ShellDocument.isResultFailCategory(thisLineRangesMap.get(range))) {
                    type = ShellTokenType.FAIL;
                } else {
                    type = null;
                }
                addToken(robotLine, RobotToken.create(partInRange, type), range.lowerEndpoint());

                start = range.upperEndpoint();
            }
            if (start < offset + line.length()) {
                addToken(robotLine, RobotToken.create(line.substring(start - offset), RobotTokenType.UNKNOWN), offset);
            }

        }
    }

    private static void addToken(final RobotLine line, final RobotToken token, final int lineStartOffset) {
        final int column = getEndColumn(line);

        final FilePosition position = new FilePosition(line.getLineNumber(), column, lineStartOffset + column);
        token.setFilePosition(position);
        line.addLineElement(token);
    }

    private static void addSeparator(final RobotLine line, final Separator separator, final int lineStartOffset) {
        final int column = getEndColumn(line);

        separator.setLineNumber(line.getLineNumber());
        separator.setStartColumn(column);
        separator.setStartOffset(lineStartOffset + column);
        line.addLineElement(separator);
    }

    private static void addLineEnding(final RobotLine line, final String delimiter, final int lineStartOffset) {
        final int column = getEndColumn(line);

        final IRobotLineElement lineEnding = EndOfLineBuilder.newInstance()
                .setEndOfLines(Constant.get(delimiter == null ? "" : delimiter))
                .setLineNumber(line.getLineNumber())
                .setStartColumn(column)
                .setStartOffset(lineStartOffset + column)
                .buildEOL();
        line.addLineElement(lineEnding);
    }

    private static int getEndColumn(final RobotLine line) {
        final List<IRobotLineElement> elements = line.getLineElements();
        return elements.isEmpty() ? 0 : elements.get(elements.size() - 1).getEndColumn();
    }
    private ExpressionType getType(final String line) {
        return ExpressionType.valueOf(line.substring(0, line.indexOf('>')).toUpperCase());
    }
}
