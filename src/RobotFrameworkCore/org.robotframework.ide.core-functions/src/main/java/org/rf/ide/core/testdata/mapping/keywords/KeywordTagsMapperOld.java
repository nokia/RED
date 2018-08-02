/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.keywords;

import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordTagsMapperOld extends KeywordTagsMapper {

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isNewerOrEqualTo(new RobotVersion(2, 9))
                && robotVersion.isOlderThan(new RobotVersion(3, 0));
    }

    @Override
    protected void createSetting(final RobotToken rt, final UserKeyword keyword) {
        if (keyword.getTags().isEmpty()) {
            keyword.addElement(new KeywordTags(rt));
        } else {
            rt.getTypes().add(1, RobotTokenType.KEYWORD_SETTING_NAME_DUPLICATION);
        }
    }
}
