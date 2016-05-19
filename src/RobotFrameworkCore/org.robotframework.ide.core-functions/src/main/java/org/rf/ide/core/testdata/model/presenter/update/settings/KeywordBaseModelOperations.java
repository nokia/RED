/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.settings;

import java.util.List;

import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.AModelElement;

public abstract class KeywordBaseModelOperations {

    protected AModelElement<?> create(final AKeywordBaseSetting<?> setting, final List<String> args,
            final String comment) {
        if (!args.isEmpty()) {
            setting.setKeywordName(args.get(0));
        }
        for (int i = 1; i < args.size(); i++) {
            setting.addArgument(args.get(i));
        }
        if (comment != null && !comment.isEmpty()) {
            setting.setComment(comment);
        }
        return setting;
    }

    protected void update(final AKeywordBaseSetting<?> setting, final int index, final String value) {
        if (index == 0) {
            setting.setKeywordName(value);
        } else if (index > 0) {
            setting.setArgument(index - 1, value);
        } else {
            setting.setComment(value);
        }
    }
}
