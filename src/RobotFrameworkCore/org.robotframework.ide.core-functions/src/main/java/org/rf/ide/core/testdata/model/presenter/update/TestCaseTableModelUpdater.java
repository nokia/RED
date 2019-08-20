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
import org.rf.ide.core.testdata.model.presenter.update.testcases.ExecRowToTestExecRowMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.LocalSettingToTestSettingMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseEmptyLineModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseExecutableRowModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseSettingModelOperation;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;

public class TestCaseTableModelUpdater implements IExecutablesTableModelUpdater<TestCase> {

    private static final List<IExecutablesStepsHolderElementOperation<TestCase>> ELEMENT_OPERATIONS = newArrayList(
            new TestCaseEmptyLineModelOperation(), new TestCaseExecutableRowModelOperation(),
            new TestCaseSettingModelOperation(ModelType.TEST_CASE_DOCUMENTATION),
            new TestCaseSettingModelOperation(ModelType.TEST_CASE_SETUP),
            new TestCaseSettingModelOperation(ModelType.TEST_CASE_TAGS),
            new TestCaseSettingModelOperation(ModelType.TEST_CASE_TEARDOWN),
            new TestCaseSettingModelOperation(ModelType.TEST_CASE_TEMPLATE),
            new TestCaseSettingModelOperation(ModelType.TEST_CASE_TIMEOUT),
            new TestCaseSettingModelOperation(ModelType.TEST_CASE_SETTING_UNKNOWN),

            new ExecRowToTestExecRowMorphOperation(),
            new LocalSettingToTestSettingMorphOperation());

    @SuppressWarnings("unchecked")
    @Override
    public RobotExecutableRow<TestCase> createExecutableRow(final TestCase testCase, final int index,
            final List<String> cells) {
        final RobotExecutableRow<?> row = createExecutableRow(cells);
        testCase.addElement(index, row);
        fixTemplateArguments(testCase, row);
        return (RobotExecutableRow<TestCase>) row;
    }

    static RobotExecutableRow<?> createExecutableRow(final List<String> cells) {
        if (cells.isEmpty()) {
            throw new IllegalArgumentException("Unable to create empty call");
        }
        final int cmtIndex = indexOfCommentStart(cells);

        final RobotExecutableRow<?> row = new RobotExecutableRow<>();
        row.setAction(RobotToken.create(cells.get(0)));
        for (int i = 1; i < cells.size(); i++) {
            if (i < cmtIndex) {
                row.addArgument(RobotToken.create(cells.get(i)));
            } else {
                row.addCommentPart(RobotToken.create(cells.get(i)));
            }
        }
        return row;
    }

    @Override
    public LocalSetting<TestCase> createSetting(final TestCase testCase, final int index,
            final List<String> cells) {
        if (cells.isEmpty()) {
            throw new IllegalArgumentException("Unable to create empty setting. There is no setting name given");
        }
        final String settingName = cells.get(0);
        if (testCase == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + settingName + " setting. There is no parent given");
        }
        final BiFunction<Integer, String, LocalSetting<TestCase>> creator = getSettingCreateOperation(testCase,
                settingName);
        if (creator == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + settingName + " setting. Operation handler is missing");
        }
        final int cmtIndex = indexOfCommentStart(cells);

        final LocalSetting<TestCase> newSetting = creator.apply(index, settingName);
        for (int i = 1; i < cells.size(); i++) {
            if (i < cmtIndex) {
                newSetting.addToken(RobotToken.create(cells.get(i)));
            } else {
                newSetting.addCommentPart(RobotToken.create(cells.get(i)));
            }
        }
        return newSetting;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RobotEmptyRow<TestCase> createEmptyLine(final TestCase testCase, final int index,
            final List<String> cells) {
        final RobotEmptyRow<?> row = createEmptyLine(cells);
        testCase.addElement(index, row);
        return (RobotEmptyRow<TestCase>) row;
    }

    static RobotEmptyRow<?> createEmptyLine(final List<String> cells) {
        final RobotEmptyRow<?> row = new RobotEmptyRow<>();

        if (!cells.isEmpty()) {
            final int cmtIndex = indexOfCommentStart(cells);
            if (cmtIndex > 0) {
                final RobotToken emptyToken = RobotToken.create(cells.get(0), RobotTokenType.EMPTY_CELL);
                emptyToken.markAsDirty();
                row.setEmpty(emptyToken);
            }

            for (int i = cmtIndex; i < cells.size(); i++) {
                row.addCommentPart(RobotToken.create(cells.get(i)));
            }
        }
        return row;
    }

    static int indexOfCommentStart(final List<String> data) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).trim().startsWith("#")) {
                return i;
            }
        }
        return data.size();
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
        final AModelElement<?> inserted = operationHandler.insert(testCase, index, modelElement);
        fixTemplateArguments(testCase, inserted);
        return inserted;
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

    private void fixTemplateArguments(final TestCase testCase, final AModelElement<?> modelElement) {
        if (modelElement instanceof RobotExecutableRow<?>) {
            final RobotExecutableRow<?> row = (RobotExecutableRow<?>) modelElement;
            row.fixTemplateArgumentsTypes(testCase.getTemplateKeywordName().isPresent(),
                    RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT);
        }
    }
}
