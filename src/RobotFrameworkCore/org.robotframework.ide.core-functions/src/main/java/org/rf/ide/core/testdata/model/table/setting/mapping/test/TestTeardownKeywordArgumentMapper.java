/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testData.model.table.setting.mapping.test;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testData.model.FilePosition;
import org.rf.ide.core.testData.model.RobotFileOutput;
import org.rf.ide.core.testData.model.table.SettingTable;
import org.rf.ide.core.testData.model.table.mapping.ElementsUtility;
import org.rf.ide.core.testData.model.table.mapping.IParsingMapper;
import org.rf.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.rf.ide.core.testData.model.table.setting.TestTeardown;
import org.rf.ide.core.testData.text.read.ParsingState;
import org.rf.ide.core.testData.text.read.RobotLine;
import org.rf.ide.core.testData.text.read.recognizer.RobotToken;
import org.rf.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestTeardownKeywordArgumentMapper implements IParsingMapper {

    private final ElementsUtility utility;
    private final ParsingStateHelper stateHelper;


    public TestTeardownKeywordArgumentMapper() {
        this.utility = new ElementsUtility();
        this.stateHelper = new ParsingStateHelper();
    }


    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        rt.getTypes().add(0,
                RobotTokenType.SETTING_TEST_TEARDOWN_KEYWORD_ARGUMENT);
        rt.setText(text);
        rt.setRaw(text);

        final SettingTable settings = robotFileOutput.getFileModel()
                .getSettingTable();
        final List<TestTeardown> teardowns = settings.getTestTeardowns();
        if (!teardowns.isEmpty()) {
            teardowns.get(teardowns.size() - 1).addArgument(rt);
        } else {
            // FIXME: some error
        }
        processingState
                .push(ParsingState.SETTING_TEST_TEARDOWN_KEYWORD_ARGUMENT);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState state = stateHelper.getCurrentStatus(processingState);
        if (state == ParsingState.SETTING_TEST_TEARDOWN) {
            final List<TestTeardown> testTeardowns = robotFileOutput.getFileModel()
                    .getSettingTable().getTestTeardowns();
            result = utility.checkIfHasAlreadyKeywordName(testTeardowns);
        } else if (state == ParsingState.SETTING_TEST_TEARDOWN_KEYWORD
                || state == ParsingState.SETTING_TEST_TEARDOWN_KEYWORD_ARGUMENT) {
            result = true;
        }

        return result;
    }

}
