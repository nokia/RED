/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting;

import org.rf.ide.core.testdata.model.ATags;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class DefaultTags extends ATags<SettingTable> {

    private static final long serialVersionUID = 1L;

    public DefaultTags(RobotToken declaration) {
        super(declaration);
    }

    @Override
    public ModelType getModelType() {
        return ModelType.DEFAULT_TAGS_SETTING;
    }

    @Override
    public IRobotTokenType getTagType() {
        return RobotTokenType.SETTING_DEFAULT_TAG;
    }
}
