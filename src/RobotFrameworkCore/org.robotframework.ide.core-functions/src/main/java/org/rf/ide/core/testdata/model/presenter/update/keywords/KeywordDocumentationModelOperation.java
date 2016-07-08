/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.keywords;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IKeywordTableElementOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;

public class KeywordDocumentationModelOperation implements IKeywordTableElementOperation {

    @Override
    public boolean isApplicable(ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_DOCUMENTATION;
    }

    @Override
    public void update(AModelElement<?> modelElement, int index, String value) {
        KeywordDocumentation keywordDocumentation = (KeywordDocumentation) modelElement;

    }

}
