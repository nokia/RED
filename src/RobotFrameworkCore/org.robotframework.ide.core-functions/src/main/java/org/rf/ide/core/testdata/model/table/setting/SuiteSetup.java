/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting;

import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class SuiteSetup extends AKeywordBaseSetting<SettingTable> {

    private static final long serialVersionUID = 1L;

    public SuiteSetup(final RobotToken declaration) {
        super(declaration);
    }

    @Override
    public ModelType getModelType() {
        return ModelType.SUITE_SETUP;
    }

    @Override
    public IRobotTokenType getKeywordNameType() {
        return RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_NAME;
    }

    @Override
    public IRobotTokenType getArgumentType() {
        return RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_ARGUMENT;
    }

    @Override
    protected RobotTokenType getDeclarationType() {
        return RobotTokenType.SETTING_SUITE_SETUP_DECLARATION;
    }
}
