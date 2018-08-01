/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.keywords;

import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordDocumentationMapper extends AKeywordSettingDeclarationMapper {

    public KeywordDocumentationMapper() {
        super(RobotTokenType.KEYWORD_SETTING_DOCUMENTATION, ParsingState.KEYWORD_SETTING_DOCUMENTATION_DECLARATION);
    }

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isNewerOrEqualTo(new RobotVersion(3, 0));
    }

    @Override
    protected void createSetting(final RobotToken rt, final UserKeyword keyword) {
        keyword.addDocumentation(new KeywordDocumentation(rt));
    }
}
