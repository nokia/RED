package org.robotframework.ide.core.testData.model.table.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class GarbageBeforeFirstTableMapper implements IParsingMapper {

    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        // nothing to do
        rt.setText(new StringBuilder(text));
        List<IRobotTokenType> types = rt.getTypes();
        if (!types.contains(RobotTokenType.START_HASH_COMMENT)
                && !types.contains(RobotTokenType.COMMENT_CONTINUE)) {
            rt.setType(RobotTokenType.UNKNOWN);
        }
        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        if (rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
            if (processingState.isEmpty()) {
                result = true;
            } else {
                ParsingState state = processingState.peek();
                result = (state == ParsingState.UNKNOWN || state == ParsingState.TRASH);
            }
        }

        return result;
    }
}
