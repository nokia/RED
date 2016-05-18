/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;

public class ModelElementUpdater {

    public void updateModelElement(final AModelElement<?> modelElement, final int index, final String value) {
        if (modelElement != null) {
            ModelType modelType = modelElement.getModelType();
            if (modelType == ModelType.SUITE_SETUP) {
                updateSuiteSetupElement(modelElement, index, value);
            }

        }
    }

    public void updateModelElementComment(final AModelElement<?> modelElement, final String comment) {
        if (modelElement != null) {
            ModelType modelType = modelElement.getModelType();
            if (modelType == ModelType.SUITE_SETUP) {
                updateSuiteSetupElementComment(modelElement, comment);
            }
        }
    }

    private void updateSuiteSetupElement(final AModelElement<?> modelElement, final int index, final String value) {
        SuiteSetup suiteSetup = (SuiteSetup) modelElement;
        if (index == 0) {
            suiteSetup.setKeywordName(value);
        } else {
            suiteSetup.setArgument(index - 1, value);
        }

    }

    private void updateSuiteSetupElementComment(final AModelElement<?> modelElement, final String value) {

    }
}
