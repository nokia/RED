/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.keywords;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IKeywordTableElementOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;

public class KeywordReturnModelOperation implements IKeywordTableElementOperation {

    @Override
    public boolean isApplicable(ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_RETURN;
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

}
