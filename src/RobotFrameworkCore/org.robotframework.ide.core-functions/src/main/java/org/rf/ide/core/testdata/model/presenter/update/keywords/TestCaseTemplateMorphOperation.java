/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.keywords;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordUnknownSettings;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class TestCaseTemplateMorphOperation extends ExecutablesStepsHolderMorphOperation<UserKeyword> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_TEMPLATE;
    }

    @Override
    public AModelElement<?> insert(final UserKeyword keyword, final int index,
            final AModelElement<?> modelElement) {
        final TestCaseTemplate template = (TestCaseTemplate) modelElement;

        final KeywordUnknownSettings unknownSetting = keyword.newUnknownSettings();
        unknownSetting.getDeclaration().setText(template.getDeclaration().getText());
        unknownSetting.addArgument(template.getKeywordName());
        for (final RobotToken arg : template.getUnexpectedTrashArguments()) {
            unknownSetting.addArgument(arg.getText());
        }
        for (final RobotToken comment : template.getComment()) {
            unknownSetting.addCommentPart(comment);
        }
        return unknownSetting;
    }

}
