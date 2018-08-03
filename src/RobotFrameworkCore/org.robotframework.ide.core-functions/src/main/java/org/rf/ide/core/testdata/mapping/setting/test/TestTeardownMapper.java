/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.test;

import org.rf.ide.core.testdata.mapping.setting.SettingDeclarationMapper;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestTeardownMapper extends SettingDeclarationMapper {

    public TestTeardownMapper() {
        super(RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION, ParsingState.SETTING_TEST_TEARDOWN);
    }

    @Override
    protected boolean addSetting(final SettingTable settingTable, final RobotToken token) {
        settingTable.addTestTeardown(new TestTeardown(token));
        return settingTable.getTestTeardowns().size() > 1;
    }
}
