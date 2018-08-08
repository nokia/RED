/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.keywords;

import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordTagsMapperOld extends KeywordSettingDeclarationMapperOld {

    public KeywordTagsMapperOld() {
        super(RobotTokenType.KEYWORD_SETTING_TAGS, ParsingState.KEYWORD_SETTING_TAGS,
                ModelType.USER_KEYWORD_TAGS, keyword -> keyword.getTags().isEmpty());
    }

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isNewerOrEqualTo(new RobotVersion(2, 9))
                && robotVersion.isOlderThan(new RobotVersion(3, 0));
    }
}
