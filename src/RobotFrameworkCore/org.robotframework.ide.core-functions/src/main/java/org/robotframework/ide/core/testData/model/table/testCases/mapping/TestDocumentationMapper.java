package org.robotframework.ide.core.testData.model.table.testCases.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestDocumentationMapper implements IParsingMapper {

    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;

        if (rt.getTypes().get(0) == RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION) {
            List<IRobotLineElement> lineElements = currentLine
                    .getLineElements();
            int size = lineElements.size();
            if (size == 1) {
                List<IRobotTokenType> types = lineElements.get(0).getTypes();
                result = (types.contains(SeparatorType.PIPE) || types
                        .contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE));
            } else {

            }
        }

        return result;
    }

}
