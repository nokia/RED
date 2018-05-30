/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.keywords;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesStepsHolderElementOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordTimeoutModelOperation implements IExecutablesStepsHolderElementOperation<UserKeyword> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_TIMEOUT;
    }

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.KEYWORD_SETTING_TIMEOUT;
    }

    @Override
    public AModelElement<UserKeyword> create(final UserKeyword userKeyword, final int index, final String settingName,
            final List<String> args, final String comment) {
        final KeywordTimeout keywordTimeout = userKeyword.newTimeout(index);
        keywordTimeout.getDeclaration().setText(settingName);

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
    public AModelElement<?> insert(final UserKeyword userKeyword, final int index,
            final AModelElement<?> modelElement) {
        userKeyword.addElement((KeywordTimeout) modelElement, index);
        return modelElement;
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
    public void update(final AModelElement<?> modelElement, final List<String> newArguments) {
        final KeywordTimeout timeout = (KeywordTimeout) modelElement;

        timeout.setTimeout(newArguments.isEmpty() ? null : RobotToken.create(newArguments.get(0)));
        final int elementsToRemove = timeout.getMessage().size();
        for (int i = 0; i < elementsToRemove; i++) {
            timeout.removeElementToken(0);
        }
        for (int i = 1; i < newArguments.size(); i++) {
            timeout.addMessagePart(i - 1, newArguments.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final UserKeyword userKeyword, final AModelElement<?> modelElement) {
        userKeyword.removeElement((AModelElement<UserKeyword>) modelElement);
    }
}
