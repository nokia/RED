/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.keywords;

import java.util.function.Predicate;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordSettingDeclarationMapperOld extends KeywordSettingDeclarationMapper {

    private final Predicate<UserKeyword> shouldBeCreated;

    public KeywordSettingDeclarationMapperOld(final RobotTokenType declarationType,
            final ParsingState newParsingState, final ModelType settingModelType,
            final Predicate<UserKeyword> shouldBeCreated) {
        super(declarationType, newParsingState, settingModelType);
        this.shouldBeCreated = shouldBeCreated;
    }

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isOlderThan(new RobotVersion(3, 0));
    }

    @Override
    protected void addElement(final RobotToken rt, final UserKeyword keyword) {
        if (shouldBeCreated.test(keyword)) {
            super.addElement(rt, keyword);
        } else {
            rt.getTypes().add(1, RobotTokenType.TEST_CASE_SETTING_NAME_DUPLICATION);
        }
    }
}
