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
import org.rf.ide.core.testdata.model.table.keywords.KeywordReturn;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordReturnModelOperation implements IExecutablesStepsHolderElementOperation<UserKeyword> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_RETURN;
    }
    
    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.KEYWORD_SETTING_RETURN;
    }

    @Override
    public AModelElement<?> create(final UserKeyword userKeyword, final String settingName, final List<String> args,
            final String comment) {
        final KeywordReturn keywordReturn = userKeyword.newReturn();
        keywordReturn.getDeclaration().setText(settingName);
        keywordReturn.getDeclaration().setRaw(settingName);

        for (int i = 0; i < args.size(); i++) {
            keywordReturn.addReturnValue(i, args.get(i));
        }
        if (comment != null && !comment.isEmpty()) {
            keywordReturn.setComment(comment);
        }
        return keywordReturn;
    }

    @Override
    public AModelElement<?> insert(final UserKeyword userKeyword, final int index,
            final AModelElement<?> modelElement) {
        userKeyword.addReturn(0, (KeywordReturn) modelElement);
        return modelElement;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final KeywordReturn keywordReturn = (KeywordReturn) modelElement;
        if (value != null) {
            keywordReturn.addReturnValue(index, value);
        } else {
            keywordReturn.removeElementToken(index);
        }
    }

    @Override
    public void update(final AModelElement<?> modelElement, final List<String> newArguments) {
        final KeywordReturn keywordReturn = (KeywordReturn) modelElement;

        int elementsToRemove = keywordReturn.getReturnValues().size();
        for (int i = 0; i < elementsToRemove; i++) {
            keywordReturn.removeElementToken(0);
        }
        for (int i = 0; i < newArguments.size(); i++) {
            keywordReturn.addReturnValue(i, newArguments.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final UserKeyword userKeyword, final AModelElement<?> modelElement) {
        userKeyword.removeUnitSettings((AModelElement<UserKeyword>) modelElement);
    }
}
