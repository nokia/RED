package org.robotframework.ide.core.testData.model.table;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.TxtRobotFileParser.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;


public class TableColumnMapper implements IParsingMapper {

    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.TABLE_HEADER_COLUMN);
        rt.setText(new StringBuilder(text));
        processingState.push(ParsingState.TABLE_HEADER_COLUMN);
        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotToken rt, Stack<ParsingState> processingState) {
        boolean result = false;
        if (!processingState.isEmpty()
                && rt.getType() != RobotTokenType.START_HASH_COMMENT) {
            ParsingState state = processingState.peek();
            result = (state == ParsingState.TEST_CASE_TABLE_HEADER
                    || state == ParsingState.SETTING_TABLE_HEADER
                    || state == ParsingState.VARIABLE_TABLE_HEADER
                    || state == ParsingState.KEYWORD_TABLE_HEADER || state == ParsingState.TABLE_HEADER_COLUMN);
        }

        return result;
    }
}
