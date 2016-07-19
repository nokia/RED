package org.rf.ide.core.testdata.model.presenter.update;

import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ETokenSeparator;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseDocumentationModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseSetupModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseTagsModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseTeardownModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseTemplateModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseTimeoutModelOperation;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseTableModelUpdater {

    private static final List<ITestCaseTableElementOperation> elementOparations = Arrays.asList(
            new TestCaseDocumentationModelOperation(), new TestCaseSetupModelOperation(),
            new TestCaseTagsModelOperation(), new TestCaseTeardownModelOperation(),
            new TestCaseTemplateModelOperation(), new TestCaseTimeoutModelOperation());

    public AModelElement<TestCase> create(final TestCase testCase, final String settingName, final String comment,
            final List<String> args) {
        if (testCase != null) {
            final ITestCaseTableElementOperation operationHandler = getOperationHandler(settingName);
            if (operationHandler != null) {
                return operationHandler.create(testCase, args, comment);
            }
        }
        return null;
    }

    public void update(final AModelElement<TestCase> modelElement, final int index, final String value) {
        if (modelElement != null) {
            final ITestCaseTableElementOperation operationHandler = getOperationHandler(modelElement.getModelType());
            if (operationHandler != null) {
                operationHandler.update(modelElement, index, value);
            }
        }
    }

    public void updateComment(final AModelElement<TestCase> modelElement, final String value) {
        if (modelElement != null) {
            CommentServiceHandler.update((ICommentHolder) modelElement, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE, value);
        }
    }

    public void remove(final TestCase testCase, final AModelElement<TestCase> modelElement) {
        if (modelElement != null) {
            final ITestCaseTableElementOperation operationHandler = getOperationHandler(modelElement.getModelType());
            if (operationHandler != null) {
                testCase.removeUnitSettings(modelElement);
            }
        }
    }

    private ITestCaseTableElementOperation getOperationHandler(final ModelType elementModelType) {
        for (final ITestCaseTableElementOperation operation : elementOparations) {
            if (operation.isApplicable(elementModelType)) {
                return operation;
            }
        }
        return null;
    }

    private ITestCaseTableElementOperation getOperationHandler(final String settingName) {
        return getOperationHandler(RobotTokenType.findTypeOfDeclarationForKeywordSettingTable(settingName));
    }

    private ITestCaseTableElementOperation getOperationHandler(final IRobotTokenType type) {
        for (final ITestCaseTableElementOperation operation : elementOparations) {
            if (operation.isApplicable(type)) {
                return operation;
            }
        }
        return null;
    }
}
