/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.mapping;

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

import com.google.common.annotations.VisibleForTesting;


public class TableHeaderColumnMapper implements IParsingMapper {

    private final ElementsUtility utility;
    private final ParsingStateHelper stateHelper;


    public TableHeaderColumnMapper() {
        this.utility = new ElementsUtility();
        this.stateHelper = new ParsingStateHelper();
    }


    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.TABLE_HEADER_COLUMN);
        rt.setText(text);
        final ParsingState state = stateHelper.getCurrentStatus(processingState);
        if (state != ParsingState.TABLE_HEADER_COLUMN) {
            processingState.push(ParsingState.TABLE_HEADER_COLUMN);
        }
        final ParsingState tableHeaderState = stateHelper
                .getNearestTableHeaderState(processingState);
        final List<TableHeader<? extends ARobotSectionTable>> headersForTable = utility
                .getKnownHeadersForTable(robotFileOutput, tableHeaderState);
        if (!headersForTable.isEmpty()) {
            final TableHeader<?> lastHeader = headersForTable.get(headersForTable.size() - 1);
            lastHeader.addColumnName(rt);
        } else {
            // FIXME: error to log
        }

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState currentState = stateHelper
                .getCurrentStatus(processingState);
        if (!processingState.isEmpty()
                && !stateHelper.isTableInsideStateInHierarchy(currentState)
                && !rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                && isNotExistLineContinueAfterHeader(currentLine)) {
            final ParsingState state = processingState.peek();
            result = (stateHelper.isTableState(state) || state == ParsingState.TABLE_HEADER_COLUMN);
        }

        return result;
    }


    @VisibleForTesting
    protected boolean isNotExistLineContinueAfterHeader(
            final RobotLine currentLine) {
        boolean result = true;
        final List<IRobotLineElement> lineElements = currentLine.getLineElements();
        for (int i = 0; i < lineElements.size() || i == 2; i++) {
            final IRobotLineElement element = lineElements.get(i);
            final List<IRobotTokenType> types = element.getTypes();
            if (types.contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)) {
                result = false;
                break;
            } else if (!(types.contains(SeparatorType.PIPE) || types
                    .contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE))) {
                break;
            }
        }

        return result;
    }
}
