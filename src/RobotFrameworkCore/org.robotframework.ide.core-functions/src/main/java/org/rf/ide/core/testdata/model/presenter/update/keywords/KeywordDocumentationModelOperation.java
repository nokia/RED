/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.keywords;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.table.LocalSetting;

public class KeywordDocumentationModelOperation extends KeywordSettingModelOperation {

    public KeywordDocumentationModelOperation() {
        super(ModelType.USER_KEYWORD_DOCUMENTATION);
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final LocalSetting<?> keywordDoc = (LocalSetting<?>) modelElement;
        final IDocumentationHolder docHolderAdapter = keywordDoc.adaptTo(IDocumentationHolder.class);

        if (value == null) {
            docHolderAdapter.clearDocumentation();
        } else if (index == 0) {
            DocumentationServiceHandler.update(docHolderAdapter, value);
        }
    }

    @Override
    public void update(final AModelElement<?> modelElement, final List<String> newArguments) {
        final LocalSetting<?> keywordDoc = (LocalSetting<?>) modelElement;
        final IDocumentationHolder docHolderAdapter = keywordDoc.adaptTo(IDocumentationHolder.class);

        if (newArguments.isEmpty()) {
            docHolderAdapter.clearDocumentation();
        } else {
            DocumentationServiceHandler.update(docHolderAdapter, newArguments.get(0));
        }
    }
}
