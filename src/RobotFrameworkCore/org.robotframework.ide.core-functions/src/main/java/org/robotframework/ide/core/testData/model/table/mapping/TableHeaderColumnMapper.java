/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class TableHeaderColumnMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public TableHeaderColumnMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.TABLE_HEADER_COLUMN);
        rt.setText(new StringBuilder(text));
        ParsingState state = utility.getCurrentStatus(processingState);
        if (state != ParsingState.TABLE_HEADER_COLUMN) {
            processingState.push(ParsingState.TABLE_HEADER_COLUMN);
        }
        ParsingState tableHeaderState = utility
                .getNearestTableHeaderState(processingState);
        List<TableHeader> headersForTable = utility.getKnownHeadersForTable(
                robotFileOutput, tableHeaderState);
        if (!headersForTable.isEmpty()) {
            TableHeader lastHeader = headersForTable
                    .get(headersForTable.size() - 1);
            lastHeader.addColumnName(rt);
        } else {
            // FIXME: error to log
        }

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        ParsingState currentState = utility.getCurrentStatus(processingState);
        if (!processingState.isEmpty()
                && !utility.isTableInsideStateInHierarchy(currentState)
                && !rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                && isNotExistLineContinueAfterHeader(currentLine)) {
            ParsingState state = processingState.peek();
            result = (utility.isTableState(state) || state == ParsingState.TABLE_HEADER_COLUMN);
        }

        return result;
    }


    @VisibleForTesting
    protected boolean isNotExistLineContinueAfterHeader(
            final RobotLine currentLine) {
        boolean result = true;
        List<IRobotLineElement> lineElements = currentLine.getLineElements();
        for (int i = 0; i < lineElements.size() || i == 2; i++) {
            IRobotLineElement element = lineElements.get(i);
            List<IRobotTokenType> types = element.getTypes();
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
