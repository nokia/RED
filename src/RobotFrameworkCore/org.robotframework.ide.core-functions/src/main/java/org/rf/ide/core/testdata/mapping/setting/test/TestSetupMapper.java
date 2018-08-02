/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.test;

import org.rf.ide.core.testdata.mapping.setting.SettingDeclarationMapper;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestSetupMapper extends SettingDeclarationMapper {

    public TestSetupMapper() {
        super(RobotTokenType.SETTING_TEST_SETUP_DECLARATION, ParsingState.SETTING_TEST_SETUP);
    }

    @Override
    protected boolean addSetting(final SettingTable settingTable, final RobotToken token) {
        settingTable.addTestSetup(new TestSetup(token));
        return settingTable.getTestSetups().size() > 1;
    }
}
