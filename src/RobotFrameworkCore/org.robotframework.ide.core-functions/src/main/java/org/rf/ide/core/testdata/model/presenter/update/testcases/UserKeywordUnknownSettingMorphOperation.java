/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.presenter.update.testcases;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordUnknownSettings;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseSetup;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTemplate;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseUnknownSettings;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class UserKeywordUnknownSettingMorphOperation extends ExecutablesStepsHolderMorphOperation<TestCase> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_SETTING_UNKNOWN;
    }

    @Override
    public AModelElement<?> insert(final TestCase testCase, final int index,
            final AModelElement<?> modelElement) {
        final KeywordUnknownSettings kwSetting = (KeywordUnknownSettings) modelElement;
        
        final RobotTokenType possibleSettingType = RobotTokenType
                .findTypeOfDeclarationForTestCaseSettingTable(kwSetting.getDeclaration().getText());

        if (possibleSettingType == RobotTokenType.TEST_CASE_SETTING_TEMPLATE) {
            final TestCaseTemplate template = testCase.newTemplate(index);
            template.getDeclaration().setText(kwSetting.getDeclaration().getText());

            if (!kwSetting.getArguments().isEmpty()) {
                template.setKeywordName(kwSetting.getArguments().get(0));
                for (int i = 1; i < kwSetting.getArguments().size(); i++) {
                    template.addUnexpectedTrashArgument(kwSetting.getArguments().get(i));
                }
            }
            for (final RobotToken comment : kwSetting.getComment()) {
                template.addCommentPart(comment);
            }
            return template;

        } else if (possibleSettingType == RobotTokenType.TEST_CASE_SETTING_SETUP) {
            final TestCaseSetup setup = testCase.newSetup(index);
            setup.getDeclaration().setText(kwSetting.getDeclaration().getText());

            if (!kwSetting.getArguments().isEmpty()) {
                setup.setKeywordName(kwSetting.getArguments().get(0));
                for (int i = 1; i < kwSetting.getArguments().size(); i++) {
                    setup.addArgument(kwSetting.getArguments().get(i));
                }
            }
            for (final RobotToken comment : kwSetting.getComment()) {
                setup.addCommentPart(comment);
            }
            return setup;

        } else {
            final TestCaseUnknownSettings unkownSetting = testCase.newUnknownSettings(index);
            unkownSetting.getDeclaration().setText(kwSetting.getDeclaration().getText());

            for (final RobotToken arg : kwSetting.getArguments()) {
                unkownSetting.addArgument(arg.getText());
            }
            for (final RobotToken comment : kwSetting.getComment()) {
                unkownSetting.addCommentPart(comment);
            }
            return unkownSetting;
        }
    }
}
