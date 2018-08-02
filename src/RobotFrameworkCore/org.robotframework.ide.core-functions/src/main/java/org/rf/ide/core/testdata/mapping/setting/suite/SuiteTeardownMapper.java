/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.suite;

import org.rf.ide.core.testdata.mapping.setting.SettingDeclarationMapper;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class SuiteTeardownMapper extends SettingDeclarationMapper {

    public SuiteTeardownMapper() {
        super(RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION, ParsingState.SETTING_SUITE_TEARDOWN);
    }

    @Override
    protected void addSetting(final SettingTable settingTable, final RobotToken token) {
        settingTable.addSuiteTeardown(new SuiteTeardown(token));
    }
}
