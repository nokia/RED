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
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordArgumentsModelOperation implements IKeywordTableElementOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_ARGUMENTS;
    }
    
    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.KEYWORD_SETTING_ARGUMENTS;
    }
    
    @Override
    public AModelElement<?> create(final UserKeyword userKeyword, final String settingName, final List<String> args,
            final String comment) {
        final KeywordArguments keywordArgs = userKeyword.newArguments();
        for (int i = 0; i < args.size(); i++) {
            keywordArgs.addArgument(i, args.get(i));
        }
        if (comment != null && !comment.isEmpty()) {
            keywordArgs.setComment(comment);
        }
        return keywordArgs;
    }

    @Override
    public void insert(final UserKeyword userKeyword, final int index, final AModelElement<?> modelElement) {
        userKeyword.addArguments(0, (KeywordArguments) modelElement);
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final KeywordArguments keywordArguments = (KeywordArguments) modelElement;
        if (value != null) {
            keywordArguments.addArgument(index, value);
        } else {
            keywordArguments.removeElementToken(index);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final UserKeyword userKeyword, final AModelElement<?> modelElement) {
        userKeyword.removeUnitSettings((AModelElement<UserKeyword>) modelElement);
    }
}
