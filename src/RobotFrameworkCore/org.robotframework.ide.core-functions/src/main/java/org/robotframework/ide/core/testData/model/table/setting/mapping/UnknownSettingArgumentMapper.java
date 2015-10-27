/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.setting.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.robotframework.ide.core.testData.model.table.setting.UnknownSetting;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class UnknownSettingArgumentMapper implements IParsingMapper {

    private final ParsingStateHelper utility;


    public UnknownSettingArgumentMapper() {
        this.utility = new ParsingStateHelper();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setText(new StringBuilder(text));
        rt.getTypes().add(0, RobotTokenType.SETTING_UNKNOWN_ARGUMENT);
        rt.setRaw(new StringBuilder(text));

        List<UnknownSetting> unknownSettings = robotFileOutput.getFileModel()
                .getSettingTable().getUnknownSettings();
        if (!unknownSettings.isEmpty()) {
            UnknownSetting unknownSetting = unknownSettings.get(unknownSettings
                    .size() - 1);
            unknownSetting.addTrash(rt);
        } else {
            // FIXME: internall error
        }

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
