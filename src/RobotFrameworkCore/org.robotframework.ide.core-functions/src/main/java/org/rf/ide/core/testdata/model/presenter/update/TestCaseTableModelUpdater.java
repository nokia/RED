/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.presenter.update;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ETokenSeparator;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseDocumentationModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseEmptyLineModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseExecutableRowModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseSetupModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseTagsModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseTeardownModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseTemplateModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseTimeoutModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseUnkownModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.UserKeywordArgumentsMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.UserKeywordDocumentationMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.UserKeywordEmptyLineMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.UserKeywordExecutableRowMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.UserKeywordReturnMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.UserKeywordTagsMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.UserKeywordTeardownMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.UserKeywordTimeoutMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.UserKeywordUnknownSettingMorphOperation;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;

public class TestCaseTableModelUpdater implements IExecutablesTableModelUpdater<TestCase> {

    private static final List<IExecutablesStepsHolderElementOperation<TestCase>> ELEMENT_OPERATIONS = newArrayList(
            new TestCaseEmptyLineModelOperation(), new TestCaseExecutableRowModelOperation(),
            new TestCaseDocumentationModelOperation(),
            new TestCaseSetupModelOperation(), new TestCaseTagsModelOperation(), new TestCaseTeardownModelOperation(),
            new TestCaseTemplateModelOperation(), new TestCaseTimeoutModelOperation(),
            new TestCaseUnkownModelOperation(), new UserKeywordEmptyLineMorphOperation(),
            new UserKeywordExecutableRowMorphOperation(),
            new UserKeywordTagsMorphOperation(), new UserKeywordDocumentationMorphOperation(),
            new UserKeywordTeardownMorphOperation(), new UserKeywordTimeoutMorphOperation(),
            new UserKeywordArgumentsMorphOperation(), new UserKeywordReturnMorphOperation(),
            new UserKeywordUnknownSettingMorphOperation());

    @Override
    public AModelElement<TestCase> createEmptyLine(final TestCase testCase, final int index, final String name) {
        final IExecutablesStepsHolderElementOperation<TestCase> operationHandler = getOperationHandler(
                ModelType.TEST_CASE_EMPTY_LINE);
        if (operationHandler == null || testCase == null) {
            throw new IllegalArgumentException("Unable to create empty line. Operation handler is missing");
        }
        final AModelElement<TestCase> row = operationHandler.create(testCase, index, name, null, null);
        testCase.addElement(row, index);
        return row;
    }

    @Override
    public AModelElement<TestCase> createSetting(final TestCase testCase, final int index, final String settingName,
            final String comment, final List<String> args) {
        final IExecutablesStepsHolderElementOperation<TestCase> operationHandler = getOperationHandler(settingName);
        if (operationHandler == null || testCase == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + settingName + " setting. Operation handler is missing");
        }
        return operationHandler.create(testCase, index, settingName, args, comment);
    }

    @Override
    public AModelElement<TestCase> createExecutableRow(final TestCase testCase, final int index, final String action,
            final String comment, final List<String> args) {
        final IExecutablesStepsHolderElementOperation<TestCase> operationHandler = getOperationHandler(
                ModelType.TEST_CASE_EXECUTABLE_ROW);
        if (operationHandler == null || testCase == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + action + " executable row. Operation handler is missing");
        }
        final AModelElement<TestCase> row = operationHandler.create(testCase, index, action, args, comment);
        testCase.addElement(row, index);
        return row;
    }

    @Override
    public void updateArgument(final AModelElement<?> modelElement, final int index, final String value) {
        final IExecutablesStepsHolderElementOperation<TestCase> operationHandler = getOperationHandler(
                modelElement.getModelType());
        if (operationHandler == null) {
            throw new IllegalArgumentException(
                    "Unable to update arguments of " + modelElement + ". Operation handler is missing");
        }
        operationHandler.update(modelElement, index, value);
    }

    @Override
    public void setArguments(final AModelElement<?> modelElement, final List<String> arguments) {
        final IExecutablesStepsHolderElementOperation<TestCase> operationHandler = getOperationHandler(
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
    public void remove(final TestCase testCase, final AModelElement<?> modelElement) {
        final IExecutablesStepsHolderElementOperation<TestCase> operationHandler = getOperationHandler(
                modelElement.getModelType());
        if (operationHandler == null) {
            throw new IllegalArgumentException("Unable to remove " + modelElement + " from "
                    + testCase.getName().getText() + " test case. Operation handler is missing");
        }
        operationHandler.remove(testCase, modelElement);
    }

    @Override
    public AModelElement<?> insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        // morph operations enables inserting settings taken from keywords elements
        final IExecutablesStepsHolderElementOperation<TestCase> operationHandler = getOperationHandler(
                modelElement.getModelType());
        if (operationHandler == null) {
            throw new IllegalArgumentException("Unable to insert " + modelElement + " into "
                    + testCase.getName().getText() + " test case. Operation handler is missing");
        }
        return operationHandler.insert(testCase, index, modelElement);
    }

    @VisibleForTesting
    IExecutablesStepsHolderElementOperation<TestCase> getOperationHandler(final ModelType elementModelType) {
        for (final IExecutablesStepsHolderElementOperation<TestCase> operation : ELEMENT_OPERATIONS) {
            if (operation.isApplicable(elementModelType)) {
                return operation;
            }
        }
        return null;
    }

    private IExecutablesStepsHolderElementOperation<TestCase> getOperationHandler(final String settingName) {
        final RobotTokenType type = RobotTokenType.findTypeOfDeclarationForTestCaseSettingTable(settingName);
        return getOperationHandler(
                type == RobotTokenType.UNKNOWN ? RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION : type);
    }

    @VisibleForTesting
    IExecutablesStepsHolderElementOperation<TestCase> getOperationHandler(final IRobotTokenType type) {
        for (final IExecutablesStepsHolderElementOperation<TestCase> operation : ELEMENT_OPERATIONS) {
            if (operation.isApplicable(type)) {
                return operation;
            }
        }
        return null;
    }
}
