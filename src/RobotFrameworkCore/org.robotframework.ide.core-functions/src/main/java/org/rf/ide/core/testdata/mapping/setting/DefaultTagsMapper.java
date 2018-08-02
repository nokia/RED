/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting;

import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class DefaultTagsMapper extends SettingDeclarationMapper {

    public DefaultTagsMapper() {
        super(RobotTokenType.SETTING_DEFAULT_TAGS_DECLARATION, ParsingState.SETTING_DEFAULT_TAGS);
    }

    @Override
    protected void addSetting(final SettingTable settingTable, final RobotToken token) {
        settingTable.addDefaultTags(new DefaultTags(token));
    }
}
