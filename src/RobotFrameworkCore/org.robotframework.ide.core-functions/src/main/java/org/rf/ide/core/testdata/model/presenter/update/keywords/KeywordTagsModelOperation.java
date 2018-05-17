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
import org.rf.ide.core.testdata.model.table.keywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordTagsModelOperation implements IExecutablesStepsHolderElementOperation<UserKeyword> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_TAGS;
    }

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.KEYWORD_SETTING_TAGS;
    }

    @Override
    public AModelElement<UserKeyword> create(final UserKeyword userKeyword, final int index, final String settingName,
            final List<String> args, final String comment) {
        final KeywordTags keywordTags = userKeyword.newTags(index);
        keywordTags.getDeclaration().setText(settingName);

        for (final String tag : args) {
            keywordTags.addTag(tag);
        }
        if (comment != null && !comment.isEmpty()) {
            keywordTags.setComment(comment);
        }
        return keywordTags;
    }

    @Override
    public AModelElement<?> insert(final UserKeyword userKeyword, final int index,
            final AModelElement<?> modelElement) {
        userKeyword.addElement((KeywordTags) modelElement, index);
        return modelElement;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final KeywordTags keywordTags = (KeywordTags) modelElement;
        if (value != null) {
            keywordTags.setTag(index, value);
        } else {
            keywordTags.removeElementToken(index);
        }
    }

    @Override
    public void update(final AModelElement<?> modelElement, final List<String> newArguments) {
        final KeywordTags tags = (KeywordTags) modelElement;

        final int elementsToRemove = tags.getTags().size();
        for (int i = 0; i < elementsToRemove; i++) {
            tags.removeElementToken(0);
        }
        for (int i = 0; i < newArguments.size(); i++) {
            tags.setTag(i, newArguments.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final UserKeyword userKeyword, final AModelElement<?> modelElement) {
        userKeyword.removeElement((AModelElement<UserKeyword>) modelElement);
    }
}
