/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting.views;

import java.util.List;

import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.ATags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class OneSettingJoinerHelper {

    public static void joinKeywordBase(final AKeywordBaseSetting<?> current,
            final List<? extends AKeywordBaseSetting<?>> base) {
        for (final AKeywordBaseSetting<?> s : base) {
            if (s.getKeywordName() != null) {
                if (current.getKeywordName() != null) {
                    current.addArgument(s.getKeywordName());
                } else {
                    current.setKeywordName(s.getKeywordName());
                }
            }

            for (final RobotToken arg : s.getArguments()) {
                current.addArgument(arg);
            }

            for (final RobotToken commentText : s.getComment()) {
                current.addCommentPart(commentText);
            }
        }
    }

    public static void joinATag(final ATags<?> currentTag, final List<? extends ATags<?>> tags) {
        for (final ATags<?> t : tags) {
            for (final RobotToken tag : t.getTags()) {
                currentTag.addTag(tag);
            }

            for (final RobotToken comment : t.getComment()) {
                currentTag.addCommentPart(comment);
            }
        }
    }
}
