/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.setting.mapping.suite;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.robotframework.ide.core.testData.model.table.setting.SuiteTeardown;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class SuiteTeardownKeywordMapper implements IParsingMapper {

    private final ElementsUtility utility;
    private final ParsingStateHelper stateHelper;


    public SuiteTeardownKeywordMapper() {
        this.utility = new ElementsUtility();
        this.stateHelper = new ParsingStateHelper();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.getTypes()
                .add(0, RobotTokenType.SETTING_SUITE_TEARDOWN_KEYWORD_NAME);
        rt.setText(new StringBuilder(text));
        rt.setRaw(new StringBuilder(text));

        SettingTable settings = robotFileOutput.getFileModel()
                .getSettingTable();
        List<SuiteTeardown> teardowns = settings.getSuiteTeardowns();
        if (!teardowns.isEmpty()) {
            teardowns.get(teardowns.size() - 1).setKeywordName(rt);
        } else {
            // FIXME: some internal error
        }
        processingState.push(ParsingState.SETTING_SUITE_TEARDOWN_KEYWORD);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        ParsingState state = stateHelper.getCurrentStatus(processingState);
        if (state == ParsingState.SETTING_SUITE_TEARDOWN) {
            List<SuiteTeardown> suiteTeardowns = robotFileOutput.getFileModel()
                    .getSettingTable().getSuiteTeardowns();
            result = !utility.checkIfHasAlreadyKeywordName(suiteTeardowns);
        }
        return result;
    }
}
