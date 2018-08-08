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
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ETokenSeparator;
import org.rf.ide.core.testdata.model.presenter.update.keywords.EmptyLineToKeywordEmptyLineMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.ExecRowToKeywordExecRowMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordDocumentationModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordEmptyLineModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordExecutableRowModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordSettingModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.LocalSettingToKeywordSettingMorphOperation;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author mmarzec
 */
public class KeywordTableModelUpdater implements IExecutablesTableModelUpdater<UserKeyword> {

    private static final List<IExecutablesStepsHolderElementOperation<UserKeyword>> ELEMENT_OPERATIONS = newArrayList(
            new KeywordEmptyLineModelOperation(), new KeywordExecutableRowModelOperation(),
            new KeywordSettingModelOperation(ModelType.USER_KEYWORD_ARGUMENTS),
            new KeywordDocumentationModelOperation(),
            new KeywordSettingModelOperation(ModelType.USER_KEYWORD_TAGS),
            new KeywordSettingModelOperation(ModelType.USER_KEYWORD_RETURN),
            new KeywordSettingModelOperation(ModelType.USER_KEYWORD_TEARDOWN),
            new KeywordSettingModelOperation(ModelType.USER_KEYWORD_TIMEOUT),
            new KeywordSettingModelOperation(ModelType.USER_KEYWORD_SETTING_UNKNOWN),

            new ExecRowToKeywordExecRowMorphOperation(), new EmptyLineToKeywordEmptyLineMorphOperation(),
            new LocalSettingToKeywordSettingMorphOperation());

    @Override
    public AModelElement<UserKeyword> createEmptyLine(final UserKeyword userKeyword, final int index, final String name) {
        final IExecutablesStepsHolderElementOperation<UserKeyword> operationHandler = getOperationHandler(
                ModelType.USER_KEYWORD_EMPTY_LINE);
        if (operationHandler == null || userKeyword == null) {
            throw new IllegalArgumentException(
                    "Unable to create empty line. Operation handler is missing");
        }
        final AModelElement<UserKeyword> row = operationHandler.create(userKeyword, index, name, null, null);
        userKeyword.addElement(index, row);
        return row;
    }

    @Override
    public AModelElement<UserKeyword> createSetting(final UserKeyword userKeyword, final int index, final String settingName,
            final String comment, final List<String> args) {

        if (userKeyword == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + settingName + " setting. There is no keyword given");
        }
        final BiFunction<Integer, String, LocalSetting<UserKeyword>> creator = getSettingCreateOperation(userKeyword,
                settingName);
        if (creator == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + settingName + " setting. Operation handler is missing");
        }
        return new KeywordSettingModelOperation(null).create(creator, index, settingName, args, comment);
    }

    @Override
    public AModelElement<UserKeyword> createExecutableRow(final UserKeyword userKeyword, final int index,
            final String action, final String comment, final List<String> args) {
        final IExecutablesStepsHolderElementOperation<UserKeyword> operationHandler = getOperationHandler(
                ModelType.USER_KEYWORD_EXECUTABLE_ROW);
        if (operationHandler == null || userKeyword == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + action + " executable row. Operation handler is missing");
        }
        final AModelElement<UserKeyword> row = operationHandler.create(userKeyword, index, action, args, comment);
        userKeyword.addElement(index, row);
        return row;
    }

    @Override
    public void updateArgument(final AModelElement<?> modelElement, final int index, final String value) {
        final IExecutablesStepsHolderElementOperation<UserKeyword> operationHandler = getOperationHandler(
                modelElement.getModelType());
        if (operationHandler == null) {
            throw new IllegalArgumentException(
                    "Unable to update arguments of " + modelElement + ". Operation handler is missing");
        }
        operationHandler.update(modelElement, index, value);
    }

    @Override
    public void setArguments(final AModelElement<?> modelElement, final List<String> arguments) {
        final IExecutablesStepsHolderElementOperation<UserKeyword> operationHandler = getOperationHandler(
                modelElement.getModelType());
        if (operationHandler == null) {
            throw new IllegalArgumentException(
                    "Unable to set arguments of " + modelElement + ". Operation handler is missing");
        }
        operationHandler.update(modelElement, arguments);
    }

    @Override
    public void updateComment(final AModelElement<?> modelElement, final String value) {
        CommentServiceHandler.update((ICommentHolder) modelElement, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE, value);
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
