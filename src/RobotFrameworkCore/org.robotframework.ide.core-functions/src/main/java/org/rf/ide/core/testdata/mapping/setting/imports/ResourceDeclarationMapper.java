/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.imports;

import org.rf.ide.core.testdata.mapping.setting.SettingDeclarationMapper;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class ResourceDeclarationMapper extends SettingDeclarationMapper {

    public ResourceDeclarationMapper() {
        super(RobotTokenType.SETTING_RESOURCE_DECLARATION, ParsingState.SETTING_RESOURCE_IMPORT);
    }

    @Override
    protected void addSetting(final SettingTable settingTable, final RobotToken token) {
        settingTable.addImported(new ResourceImport(token));
    }
}
