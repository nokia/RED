/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordArgumentsModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordDocumentationModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordReturnModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordTagsModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordTeardownModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordTimeoutModelOperation;

/**
 * @author mmarzec
 *
 */
public class KeywordTableModelUpdater {

    private static final List<IKeywordTableElementOperation> elementOparations = Arrays.asList(
            new KeywordArgumentsModelOperation(), new KeywordDocumentationModelOperation(),
            new KeywordTagsModelOperation(), new KeywordReturnModelOperation(), new KeywordTeardownModelOperation(),
            new KeywordTimeoutModelOperation());

    public void update(final AModelElement<?> modelElement, final int index, final String value) {

        if (modelElement != null) {
            final IKeywordTableElementOperation operationHandler = getOperationHandler(modelElement.getModelType());
            if (operationHandler != null) {
                operationHandler.update(modelElement, index, value);
            }
        }
    }

    private IKeywordTableElementOperation getOperationHandler(final ModelType elementModelType) {
        for (final IKeywordTableElementOperation operation : elementOparations) {
            if (operation.isApplicable(elementModelType)) {
                return operation;
            }
        }

        return null;
    }

}
