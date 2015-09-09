/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.setting.mapping.test;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.setting.TestSetup;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestSetupKeywordArgumentMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public TestSetupKeywordArgumentMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.SETTING_TEST_SETUP_KEYWORD_ARGUMENT);
        rt.setText(new StringBuilder(text));

        SettingTable settings = robotFileOutput.getFileModel()
                .getSettingTable();
        List<TestSetup> setups = settings.getTestSetups();
        if (!setups.isEmpty()) {
            setups.get(setups.size() - 1).addArgument(rt);
        } else {
            // FIXME: some error
        }
        processingState.push(ParsingState.SETTING_TEST_SETUP_KEYWORD_ARGUMENT);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        ParsingState state = utility.getCurrentStatus(processingState);
        if (state == ParsingState.SETTING_TEST_SETUP) {
            List<TestSetup> testSetups = robotFileOutput.getFileModel()
                    .getSettingTable().getTestSetups();
            result = utility.checkIfHasAlreadyKeywordName(testSetups);
        } else if (state == ParsingState.SETTING_TEST_SETUP_KEYWORD
                || state == ParsingState.SETTING_TEST_SETUP_KEYWORD_ARGUMENT) {
            result = true;
        }

        return result;
    }

}
