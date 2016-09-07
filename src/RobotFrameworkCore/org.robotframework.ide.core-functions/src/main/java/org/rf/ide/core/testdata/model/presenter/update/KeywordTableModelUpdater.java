/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ETokenSeparator;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordArgumentsModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordDocumentationModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordExecutableRowModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordReturnModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordTagsModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordTeardownModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordTimeoutModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.KeywordUnknownModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.TestCaseDocumentationMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.TestCaseExecutableRowMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.TestCaseSetupMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.TestCaseTagsMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.TestCaseTeardownMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.TestCaseTemplateMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.TestCaseTimeoutMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.keywords.TestCaseUnknownSettingMorphOperation;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author mmarzec
 */
public class KeywordTableModelUpdater implements IExecutablesTableModelUpdater<UserKeyword> {

    private static final List<IExecutablesStepsHolderElementOperation<UserKeyword>> elementOparations = Arrays.asList(
            new KeywordExecutableRowModelOperation(), new KeywordArgumentsModelOperation(),
            new KeywordDocumentationModelOperation(), new KeywordTagsModelOperation(),
            new KeywordReturnModelOperation(), new KeywordTeardownModelOperation(), new KeywordTimeoutModelOperation(),
            new KeywordUnknownModelOperation(), new TestCaseDocumentationMorphOperation(),
            new TestCaseExecutableRowMorphOperation(), new TestCaseSetupMorphOperation(),
            new TestCaseTagsMorphOperation(), new TestCaseTeardownMorphOperation(),
            new TestCaseTemplateMorphOperation(), new TestCaseTimeoutMorphOperation(),
            new TestCaseUnknownSettingMorphOperation());

    @Override
    public AModelElement<?> createSetting(final UserKeyword userKeyword, final String settingName,
            final String comment, final List<String> args) {
        final IExecutablesStepsHolderElementOperation<UserKeyword> operationHandler = getOperationHandler(settingName);
        if (operationHandler == null || userKeyword == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + settingName + " setting. Operation handler is missing");
        }
        return operationHandler.create(userKeyword, settingName, args, comment);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AModelElement<?> createExecutableRow(final UserKeyword userKeyword, final int index,
            final String action, final String comment, final List<String> args) {
        final IExecutablesStepsHolderElementOperation<UserKeyword> operationHandler = getOperationHandler(
                ModelType.USER_KEYWORD_EXECUTABLE_ROW);
        if (operationHandler == null || userKeyword == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + action + " executable row. Operation handler is missing");
        }
        final AModelElement<?> row = operationHandler.create(userKeyword, action, args, comment);
        userKeyword.addKeywordExecutionRow((RobotExecutableRow<UserKeyword>) row, index);
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
    public void remove(final UserKeyword userKeyword, final AModelElement<?> modelElement) {
        final IExecutablesStepsHolderElementOperation<UserKeyword> operationHandler = getOperationHandler(
                modelElement.getModelType());
        if (operationHandler == null) {
            throw new IllegalArgumentException("Unable to remove " + modelElement + " from "
                    + userKeyword.getName().getText() + " keyword. Operation handler is missing");
        }
        operationHandler.remove(userKeyword, modelElement);
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
        for (final IExecutablesStepsHolderElementOperation<UserKeyword> operation : elementOparations) {
            if (operation.isApplicable(elementModelType)) {
                return operation;
            }
        }
        return null;
    }

    private IExecutablesStepsHolderElementOperation<UserKeyword> getOperationHandler(final String settingName) {
        final RobotTokenType type = RobotTokenType.findTypeOfDeclarationForKeywordSettingTable(settingName);
        return getOperationHandler(
                type == RobotTokenType.UNKNOWN ? RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION : type);
    }

    @VisibleForTesting
    IExecutablesStepsHolderElementOperation<UserKeyword> getOperationHandler(final IRobotTokenType type) {
        for (final IExecutablesStepsHolderElementOperation<UserKeyword> operation : elementOparations) {
            if (operation.isApplicable(type)) {
                return operation;
            }
        }
        return null;
    }
}
