package org.robotframework.ide.core.testData.model.table.mapping;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.IRobotFileOutput;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public interface IParsingMapper {

    RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final IRobotFileOutput robotFileOutput, final RobotToken rt,
            final FilePosition fp, final String text);


    boolean checkIfCanBeMapped(final IRobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, String text,
            final Stack<ParsingState> processingState);
}
