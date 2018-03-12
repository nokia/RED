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
import org.rf.ide.core.testdata.model.table.keywords.KeywordUnknownSettings;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordUnknownModelOperation implements IExecutablesStepsHolderElementOperation<UserKeyword> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_SETTING_UNKNOWN;
    }

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION;
    }

    @Override
    public AModelElement<UserKeyword> create(final UserKeyword userKeyword, final int index, final String settingName,
            final List<String> args, final String comment) {
        final KeywordUnknownSettings unknown = userKeyword.newUnknownSettings(index);
        unknown.getDeclaration().setText(settingName);
        unknown.getDeclaration().setRaw(settingName);

        for (final String arg : args) {
            unknown.addArgument(arg);
        }
        if (comment != null && !comment.isEmpty()) {
            unknown.setComment(comment);
        }
        return unknown;
    }

    @Override
    public AModelElement<?> insert(final UserKeyword userKeyword, final int index,
            final AModelElement<?> modelElement) {
        userKeyword.addElement((KeywordUnknownSettings) modelElement, index);
        return modelElement;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final KeywordUnknownSettings unknown = (KeywordUnknownSettings) modelElement;

        if (value != null) {
            unknown.addArgument(index, value);
        } else {
            unknown.removeElementToken(index);
        }
    }

    @Override
    public void update(final AModelElement<?> modelElement, final List<String> newArguments) {
        final KeywordUnknownSettings unknown = (KeywordUnknownSettings) modelElement;

        final int elementsToRemove = unknown.getArguments().size();
        for (int i = 0; i < elementsToRemove; i++) {
            unknown.removeElementToken(0);
        }
        for (int i = 0; i < newArguments.size(); i++) {
            unknown.addArgument(i, newArguments.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final UserKeyword userKeyword, final AModelElement<?> modelElement) {
        userKeyword.removeElement((AModelElement<UserKeyword>) modelElement);
    }
}
