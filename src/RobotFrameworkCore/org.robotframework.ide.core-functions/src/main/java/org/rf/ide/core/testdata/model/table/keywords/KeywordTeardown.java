/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class KeywordTeardown extends AKeywordBaseSetting<UserKeyword> {

    public KeywordTeardown(final RobotToken declaration) {
        super(declaration);
    }

    @Override
    public ModelType getModelType() {
        return ModelType.USER_KEYWORD_TEARDOWN;
    }

    @Override
    protected List<AKeywordBaseSetting<UserKeyword>> getAllThisKindSettings() {
        final List<AKeywordBaseSetting<UserKeyword>> settings = new ArrayList<>(0);
        settings.addAll(getParent().getTeardowns());

        return settings;
    }
}
