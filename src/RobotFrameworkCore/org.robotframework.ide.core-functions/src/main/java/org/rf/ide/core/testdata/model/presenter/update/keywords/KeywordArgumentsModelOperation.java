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
    public AModelElement<?> create(final UserKeyword userKeyword, final List<String> args, final String comment) {
        return null;
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

    @Override
    public AModelElement<?> createCopy(final AModelElement<?> modelElement) {
        return ((KeywordArguments) modelElement).copy();
    }

    @Override
    public void updateParent(final UserKeyword userKeyword, final AModelElement<?> modelElement) {
        userKeyword.addArguments((KeywordArguments) modelElement);
    }

}
