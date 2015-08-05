package org.robotframework.ide.core.testData.model.table.setting.mapping;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class UnknownSettingArgumentMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public UnknownSettingArgumentMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setText(new StringBuilder(text));
        rt.setType(RobotTokenType.SETTING_UNKNOWN_ARGUMENT);

        processingState.push(ParsingState.SETTING_UNKNOWN_TRASH_ELEMENT);
        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        ParsingState currentState = utility.getCurrentStatus(processingState);

        return (currentState == ParsingState.SETTING_UNKNOWN || currentState == ParsingState.SETTING_UNKNOWN_TRASH_ELEMENT);
    }

}
