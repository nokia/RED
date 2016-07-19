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
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordTimeoutModelOperation implements IKeywordTableElementOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_TIMEOUT;
    }
    
    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.KEYWORD_SETTING_TIMEOUT;
    }

    @Override
    public AModelElement<?> create(final UserKeyword userKeyword, final List<String> args, final String comment) {
        final KeywordTimeout keywordTimeout = userKeyword.newTimeout();
        if (!args.isEmpty()) {
            keywordTimeout.setTimeout(args.get(0));
            for (int i = 1; i < args.size(); i++) {
                keywordTimeout.addMessagePart(i - 1, args.get(i));
            }
        }
        if (comment != null && !comment.isEmpty()) {
            keywordTimeout.setComment(comment);
        }
        return keywordTimeout;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final KeywordTimeout keywordTimeout = (KeywordTimeout) modelElement;

        if (index == 0) {
            keywordTimeout.setTimeout(value != null ? value : "");
        } else if (index > 0) {
            if (value != null) {
                keywordTimeout.addMessagePart(index - 1, value);
            } else {
                keywordTimeout.removeElementToken(index - 1);
            }
        }
    }

    @Override
    public AModelElement<?> createCopy(final AModelElement<?> modelElement) {
        return ((KeywordTimeout) modelElement).copy();
    }
    
    @Override
    public void updateParent(final UserKeyword userKeyword, final AModelElement<?> modelElement) {
        userKeyword.addTimeout((KeywordTimeout) modelElement);
    }
}
