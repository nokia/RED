/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class SuiteSetup extends AKeywordBaseSetting<SettingTable> {

    public SuiteSetup(final RobotToken declaration) {
        super(declaration);
    }

    @Override
    public ModelType getModelType() {
        return ModelType.SUITE_SETUP;
    }

    @Override
    protected List<AKeywordBaseSetting<SettingTable>> getAllThisKindSettings() {
        final List<AKeywordBaseSetting<SettingTable>> settings = new ArrayList<>(0);
        settings.addAll(getParent().getSuiteSetups());

        return settings;
    }
}
