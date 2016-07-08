/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.keywords;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IKeywordTableElementOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;

public class KeywordTimeoutModelOperation implements IKeywordTableElementOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_TIMEOUT;
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

}
