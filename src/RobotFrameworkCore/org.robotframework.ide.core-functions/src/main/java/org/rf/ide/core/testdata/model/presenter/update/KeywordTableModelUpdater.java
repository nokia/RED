/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.function.BiFunction;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.keywords.ExecRowToKeywordExecRowMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordEmptyLineModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordExecutableRowModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordSettingModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.LocalSettingToKeywordSettingMorphOperation;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author mmarzec
 */
public class KeywordTableModelUpdater implements IExecutablesTableModelUpdater<UserKeyword> {

    private static final List<IExecutablesStepsHolderElementOperation<UserKeyword>> ELEMENT_OPERATIONS = newArrayList(
            new KeywordEmptyLineModelOperation(), new KeywordExecutableRowModelOperation(),
            new KeywordSettingModelOperation(ModelType.USER_KEYWORD_ARGUMENTS),
            new KeywordSettingModelOperation(ModelType.USER_KEYWORD_DOCUMENTATION),
            new KeywordSettingModelOperation(ModelType.USER_KEYWORD_TAGS),
            new KeywordSettingModelOperation(ModelType.USER_KEYWORD_RETURN),
            new KeywordSettingModelOperation(ModelType.USER_KEYWORD_TEARDOWN),
            new KeywordSettingModelOperation(ModelType.USER_KEYWORD_TIMEOUT),
            new KeywordSettingModelOperation(ModelType.USER_KEYWORD_SETTING_UNKNOWN),

            new ExecRowToKeywordExecRowMorphOperation(),
            new LocalSettingToKeywordSettingMorphOperation());

    @SuppressWarnings("unchecked")
    @Override
    public AModelElement<UserKeyword> createExecutableRow(final UserKeyword userKeyword, final int index,
            final List<String> cells) {
        final RobotExecutableRow<?> row = TestCaseTableModelUpdater.createExecutableRow(cells);
        userKeyword.addElement(index, row);
        return (AModelElement<UserKeyword>) row;
    }

    @Override
    public AModelElement<UserKeyword> createSetting(final UserKeyword userKeyword, final int index,
            final List<String> cells) {
        if (cells.isEmpty()) {
            throw new IllegalArgumentException("Unable to create empty setting. There is no setting name given");
        }
        final String settingName = cells.get(0);
        if (userKeyword == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + settingName + " setting. There is no parent given");
        }
        final BiFunction<Integer, String, LocalSetting<UserKeyword>> creator = getSettingCreateOperation(userKeyword,
                settingName);
        if (creator == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + settingName + " setting. Operation handler is missing");
        }
        final int cmtIndex = TestCaseTableModelUpdater.indexOfCommentStart(cells);

        final LocalSetting<UserKeyword> newSetting = creator.apply(index, settingName);
        for (int i = 1; i < cells.size(); i++) {
            if (i < cmtIndex) {
                newSetting.addToken(RobotToken.create(cells.get(i)));
            } else {
                newSetting.addCommentPart(RobotToken.create(cells.get(i)));
            }
        }
        return newSetting;
    }

    private BiFunction<Integer, String, LocalSetting<UserKeyword>> getSettingCreateOperation(final UserKeyword userKeyword,
            final String settingName) {
        final RobotTokenType type = RobotTokenType.findTypeOfDeclarationForKeywordSettingTable(settingName);
        switch (type) {
            case KEYWORD_SETTING_ARGUMENTS: return userKeyword::newArguments;
            case KEYWORD_SETTING_RETURN: return userKeyword::newReturn;
            case KEYWORD_SETTING_DOCUMENTATION: return userKeyword::newDocumentation;
            case KEYWORD_SETTING_TAGS: return userKeyword::newTags;
            case KEYWORD_SETTING_TEARDOWN: return userKeyword::newTeardown;
            case KEYWORD_SETTING_TIMEOUT: return userKeyword::newTimeout;
            default: return userKeyword::newUnknownSetting;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public AModelElement<UserKeyword> createEmptyLine(final UserKeyword userKeyword, final int index,
            final List<String> cells) {
        final RobotEmptyRow<?> row = TestCaseTableModelUpdater.createEmptyLine(cells);
        userKeyword.addElement(index, row);
        return (RobotEmptyRow<UserKeyword>) row;
    }

    @Override
    public AModelElement<?> insert(final UserKeyword userKeyword, final int index,
            final AModelElement<?> modelElement) {
        final IExecutablesStepsHolderElementOperation<UserKeyword> operationHandler = getOperationHandler(
                modelElement.getModelType());
        if (operationHandler == null) {
            throw new IllegalArgumentException("Unable to insert " + modelElement + " into "
                    + userKeyword.getName().getText() + " keyword. Operation handler is missing");
        }
        return operationHandler.insert(userKeyword, index, modelElement);
    }

    @VisibleForTesting
    IExecutablesStepsHolderElementOperation<UserKeyword> getOperationHandler(final ModelType elementModelType) {
        for (final IExecutablesStepsHolderElementOperation<UserKeyword> operation : ELEMENT_OPERATIONS) {
            if (operation.isApplicable(elementModelType)) {
                return operation;
            }
        }
        return null;
    }

    @VisibleForTesting
    IExecutablesStepsHolderElementOperation<UserKeyword> getOperationHandler(final IRobotTokenType type) {
        for (final IExecutablesStepsHolderElementOperation<UserKeyword> operation : ELEMENT_OPERATIONS) {
            if (operation.isApplicable(type)) {
                return operation;
            }
        }
        return null;
    }
}
