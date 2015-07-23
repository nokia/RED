package org.robotframework.ide.core.testData.model;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.table.IParsingMapper;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.TxtRobotFileParser.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;


public class HashCommentMapper implements IParsingMapper {

    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        if (rt.getType() != RobotTokenType.START_HASH_COMMENT) {
            rt.setType(RobotTokenType.COMMENT_CONTINUE);
        }

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotToken rt, Stack<ParsingState> processingState) {
        boolean result = false;
        if (rt.getType() == RobotTokenType.START_HASH_COMMENT) {
            processingState.push(ParsingState.COMMENT);
            result = true;
        } else if (!processingState.isEmpty()) {
            ParsingState state = processingState.peek();
            result = (state == ParsingState.COMMENT);
        }

        return result;
    }
}
