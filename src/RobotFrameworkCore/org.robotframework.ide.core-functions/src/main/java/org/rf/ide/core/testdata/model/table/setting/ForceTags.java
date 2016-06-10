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

public class ForceTags extends ATags<SettingTable> {

    private static final long serialVersionUID = 1L;

    public ForceTags(RobotToken declaration) {
        super(declaration);
    }

    @Override
    public ModelType getModelType() {
        return ModelType.FORCE_TAGS_SETTING;
    }

    @Override
    public IRobotTokenType getTagType() {
        return RobotTokenType.SETTING_FORCE_TAG;
    }
}
