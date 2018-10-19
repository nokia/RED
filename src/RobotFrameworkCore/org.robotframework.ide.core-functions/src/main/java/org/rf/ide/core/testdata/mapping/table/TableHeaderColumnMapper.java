/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;


public class TableHeaderColumnMapper implements IParsingMapper {

    private final ElementsUtility utility = new ElementsUtility();

    private final ParsingStateHelper stateHelper = new ParsingStateHelper();

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {

        final ParsingState currentState = stateHelper.getCurrentState(processingState);
        if (!processingState.isEmpty() && currentState != ParsingState.COMMENT_TABLE_HEADER
                && !stateHelper.isTableInsideStateInHierarchy(currentState)
                && !rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                && lineContinueDoesNotExistAfterHeader(currentLine)) {

            final ParsingState state = processingState.peek();
            return stateHelper.isTableHeaderState(state) || state == ParsingState.TABLE_HEADER_COLUMN;
        }
        return false;
    }

    private boolean lineContinueDoesNotExistAfterHeader(final RobotLine currentLine) {
        final List<IRobotLineElement> lineElements = currentLine.getLineElements();

        for (int i = 0; i < lineElements.size() || i == 2; i++) {
            final List<IRobotTokenType> types = lineElements.get(i).getTypes();
            if (types.contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)) {
                return false;
            } else if (!(types.contains(SeparatorType.PIPE)
                    || types.contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE))) {
                return true;
            }
        }
        return true;
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.TABLE_HEADER_COLUMN);
        rt.setText(text);

        final ParsingState state = stateHelper.getCurrentState(processingState);
        if (state != ParsingState.TABLE_HEADER_COLUMN) {
            processingState.push(ParsingState.TABLE_HEADER_COLUMN);
        }
        final ParsingState tableHeaderState = stateHelper.getFirstTableHeaderState(processingState);
        final List<TableHeader<? extends ARobotSectionTable>> headersForTable = utility
                .getKnownHeadersForTable(robotFileOutput, tableHeaderState);
        if (!headersForTable.isEmpty()) {
            final TableHeader<?> lastHeader = headersForTable.get(headersForTable.size() - 1);
            lastHeader.addColumnName(rt);
        }
        return rt;
    }
}
