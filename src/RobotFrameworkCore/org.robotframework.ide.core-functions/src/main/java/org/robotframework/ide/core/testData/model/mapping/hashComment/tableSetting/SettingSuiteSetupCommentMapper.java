/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.setting.SuiteSetup;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class SettingSuiteSetupCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_SUITE_SETUP
                || state == ParsingState.SETTING_SUITE_SETUP_KEYWORD || state == ParsingState.SETTING_SUITE_SETUP_KEYWORD_ARGUMENT);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            RobotFile fileModel) {
        List<SuiteSetup> suiteSetups = fileModel.getSettingTable()
                .getSuiteSetups();
        if (!suiteSetups.isEmpty()) {
            SuiteSetup suiteSetup = suiteSetups.get(suiteSetups.size() - 1);
            suiteSetup.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }
}
