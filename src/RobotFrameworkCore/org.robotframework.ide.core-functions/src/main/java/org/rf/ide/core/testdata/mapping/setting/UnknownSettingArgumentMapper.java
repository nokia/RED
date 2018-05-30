/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.setting.UnknownSetting;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class UnknownSettingArgumentMapper implements IParsingMapper {

    private final ParsingStateHelper utility;

    public UnknownSettingArgumentMapper() {
        this.utility = new ParsingStateHelper();
    }

    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        rt.getTypes().add(0, RobotTokenType.SETTING_UNKNOWN_ARGUMENT);
        rt.setText(text);

        final List<UnknownSetting> unknownSettings = robotFileOutput.getFileModel()
                .getSettingTable().getUnknownSettings();
        if (!unknownSettings.isEmpty()) {
            final UnknownSetting unknownSetting = unknownSettings.get(unknownSettings
                    .size() - 1);
            unknownSetting.addTrash(rt);
        } else {
            // FIXME: internal error
        }

        processingState.push(ParsingState.SETTING_UNKNOWN_TRASH_ELEMENT);
        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        final ParsingState currentState = utility.getCurrentStatus(processingState);

        return (currentState == ParsingState.SETTING_UNKNOWN
                || currentState == ParsingState.SETTING_UNKNOWN_TRASH_ELEMENT);
    }

}
