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
import org.rf.ide.core.testdata.model.presenter.update.testcases.EmptyLineToTestEmptyLineMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.ExecRowToTestExecRowMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.LocalSettingToTestSettingMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseDocumentationModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseEmptyLineModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseExecutableRowModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseSettingModelOperation;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;

public class TestCaseTableModelUpdater implements IExecutablesTableModelUpdater<TestCase> {

    private static final List<IExecutablesStepsHolderElementOperation<TestCase>> ELEMENT_OPERATIONS = newArrayList(
            new TestCaseEmptyLineModelOperation(), new TestCaseExecutableRowModelOperation(),
            new TestCaseDocumentationModelOperation(), new TestCaseSettingModelOperation(ModelType.TEST_CASE_SETUP),
            new TestCaseSettingModelOperation(ModelType.TEST_CASE_TAGS),
            new TestCaseSettingModelOperation(ModelType.TEST_CASE_TEARDOWN),
            new TestCaseSettingModelOperation(ModelType.TEST_CASE_TEMPLATE),
            new TestCaseSettingModelOperation(ModelType.TEST_CASE_TIMEOUT),
            new TestCaseSettingModelOperation(ModelType.TEST_CASE_SETTING_UNKNOWN),

            new ExecRowToTestExecRowMorphOperation(), new EmptyLineToTestEmptyLineMorphOperation(),
            new LocalSettingToTestSettingMorphOperation());

    @Override
    public AModelElement<TestCase> createEmptyLine(final TestCase testCase, final int index, final String name) {
        final IExecutablesStepsHolderElementOperation<TestCase> operationHandler = getOperationHandler(
                ModelType.TEST_CASE_EMPTY_LINE);
        if (operationHandler == null || testCase == null) {
            throw new IllegalArgumentException("Unable to create empty line. Operation handler is missing");
        }
        final AModelElement<TestCase> row = operationHandler.create(testCase, index, name, null, null);
        testCase.addElement(index, row);
        return row;
    }

    @Override
    public AModelElement<TestCase> createSetting(final TestCase testCase, final int index, final String settingName,
            final String comment, final List<String> args) {

        if (testCase == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + settingName + " setting. There is no test case given");
        }
        final BiFunction<Integer, String, LocalSetting<TestCase>> creator = getSettingCreateOperation(testCase,
                settingName);
        if (creator == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + settingName + " setting. Operation handler is missing");
        }
        return new TestCaseSettingModelOperation(null).create(creator, index, settingName, args, comment);
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
        testCase.addElement(index, row);
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

    private BiFunction<Integer, String, LocalSetting<TestCase>> getSettingCreateOperation(final TestCase testCase,
            final String settingName) {
        final RobotTokenType type = RobotTokenType.findTypeOfDeclarationForTestCaseSettingTable(settingName);
        switch (type) {
            case TEST_CASE_SETTING_SETUP: return testCase::newSetup;
            case TEST_CASE_SETTING_TEARDOWN: return testCase::newTeardown;
            case TEST_CASE_SETTING_DOCUMENTATION: return testCase::newDocumentation;
            case TEST_CASE_SETTING_TAGS_DECLARATION: return testCase::newTags;
            case TEST_CASE_SETTING_TIMEOUT: return testCase::newTimeout;
            case TEST_CASE_SETTING_TEMPLATE: return testCase::newTemplate;
            default: return testCase::newUnknownSetting;
        }
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
