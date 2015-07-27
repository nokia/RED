package org.robotframework.ide.core.testData.model.table.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.TxtRobotFileParser.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;


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
        rt.setType(RobotTokenType.TABLE_HEADER_COLUMN);
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
            RobotLine currentLine, RobotToken rt,
            Stack<ParsingState> processingState) {
        boolean result = false;
        if (!processingState.isEmpty()
                && rt.getType() != RobotTokenType.START_HASH_COMMENT) {
            ParsingState state = processingState.peek();
            result = (utility.isTableState(state) || state == ParsingState.TABLE_HEADER_COLUMN);
        }

        return result;
    }

}
