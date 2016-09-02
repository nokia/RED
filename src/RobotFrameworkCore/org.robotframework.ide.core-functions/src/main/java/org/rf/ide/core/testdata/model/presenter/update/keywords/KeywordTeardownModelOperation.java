/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.keywords;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IKeywordTableElementOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordTeardownModelOperation implements IKeywordTableElementOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_TEARDOWN;
    }
    
    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.KEYWORD_SETTING_TEARDOWN;
    }

    @Override
    public AModelElement<?> create(final UserKeyword userKeyword, final String settingName, final List<String> args,
            final String comment) {
        final KeywordTeardown keywordTeardown = userKeyword.newTeardown();
        if (!args.isEmpty()) {
            keywordTeardown.setKeywordName(args.get(0));
            for (int i = 1; i < args.size(); i++) {
                keywordTeardown.addArgument(args.get(i));
            }
        }
        if (comment != null && !comment.isEmpty()) {
            keywordTeardown.setComment(comment);
        }
        return keywordTeardown;
    }

    @Override
    public void insert(final UserKeyword userKeyword, final int index, final AModelElement<?> modelElement) {
        userKeyword.addTeardown(0, (KeywordTeardown) modelElement);
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final KeywordTeardown keywordTeardown = (KeywordTeardown) modelElement;

        if (index == 0) {
            keywordTeardown.setKeywordName(value != null ? value : "");
        } else if (index > 0) {
            if (value != null) {
                keywordTeardown.setArgument(index - 1, value);
            } else {
                keywordTeardown.removeElementToken(index - 1);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final UserKeyword userKeyword, final AModelElement<?> modelElement) {
        userKeyword.removeUnitSettings((AModelElement<UserKeyword>) modelElement);
    }
}
