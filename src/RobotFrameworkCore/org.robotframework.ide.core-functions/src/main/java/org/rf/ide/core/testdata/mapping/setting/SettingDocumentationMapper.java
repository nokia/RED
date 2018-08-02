/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting;

import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class SettingDocumentationMapper extends SettingDeclarationMapper {

    public SettingDocumentationMapper() {
        super(RobotTokenType.SETTING_DOCUMENTATION_DECLARATION, ParsingState.SETTING_DOCUMENTATION);
    }

    @Override
    protected boolean addSetting(final SettingTable settingTable, final RobotToken token) {
        settingTable.addDocumentation(new SuiteDocumentation(token));
        return settingTable.getDocumentation().size() > 1;
    }
}
