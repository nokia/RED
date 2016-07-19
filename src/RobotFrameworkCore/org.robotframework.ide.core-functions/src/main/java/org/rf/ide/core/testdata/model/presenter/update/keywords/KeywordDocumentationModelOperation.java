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
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordDocumentationModelOperation implements IKeywordTableElementOperation {

    @Override
    public boolean isApplicable(ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_DOCUMENTATION;
    }
    
    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.KEYWORD_SETTING_DOCUMENTATION;
    }
    
    @Override
    public AModelElement<?> create(final UserKeyword userKeyword, final List<String> args, final String comment) {
        return null;
    }

    @Override
    public void update(AModelElement<?> modelElement, int index, String value) {
        KeywordDocumentation keywordDocumentation = (KeywordDocumentation) modelElement;

    }
    
    @Override
    public AModelElement<?> createCopy(final AModelElement<?> modelElement) {
        return ((KeywordDocumentation) modelElement).copy();
    }
    
    @Override
    public void updateParent(final UserKeyword userKeyword, final AModelElement<?> modelElement) {
        userKeyword.addDocumentation((KeywordDocumentation) modelElement);
    }

}
