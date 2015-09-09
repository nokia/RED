/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.setting.TestSetup;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class SettingTestSetupCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_TEST_SETUP
                || state == ParsingState.SETTING_TEST_SETUP_KEYWORD || state == ParsingState.SETTING_TEST_SETUP_KEYWORD_ARGUMENT);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            RobotFile fileModel) {
        List<TestSetup> testSetups = fileModel.getSettingTable()
                .getTestSetups();
        if (!testSetups.isEmpty()) {
            TestSetup testSetup = testSetups.get(testSetups.size() - 1);
            testSetup.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }

}
