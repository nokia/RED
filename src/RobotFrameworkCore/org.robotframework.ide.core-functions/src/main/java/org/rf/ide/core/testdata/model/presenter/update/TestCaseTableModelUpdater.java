package org.rf.ide.core.testdata.model.presenter.update;

import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ETokenSeparator;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseDocumentationModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseExecutableRowModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseSetupModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseTagsModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseTeardownModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseTemplateModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseTimeoutModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.TestCaseUnkownModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.UserKeywordArgumentsMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.testcases.UserKeywordDocumentationMorphOperation;
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

public class TestCaseTableModelUpdater {

    private static final List<ITestCaseTableElementOperation> elementOparations = Arrays.asList(
            new TestCaseExecutableRowModelOperation(), new TestCaseDocumentationModelOperation(),
            new TestCaseSetupModelOperation(), new TestCaseTagsModelOperation(), new TestCaseTeardownModelOperation(),
            new TestCaseTemplateModelOperation(), new TestCaseTimeoutModelOperation(),
            new TestCaseUnkownModelOperation(), new UserKeywordExecutableRowMorphOperation(),
            new UserKeywordTagsMorphOperation(), new UserKeywordDocumentationMorphOperation(),
            new UserKeywordTeardownMorphOperation(), new UserKeywordTimeoutMorphOperation(),
            new UserKeywordArgumentsMorphOperation(), new UserKeywordReturnMorphOperation(),
            new UserKeywordUnknownSettingMorphOperation());

    public AModelElement<?> createSetting(final TestCase testCase, final String settingName,
            final String comment, final List<String> args) {
        if (testCase == null) {
            return null;
        }
        final ITestCaseTableElementOperation operationHandler = getOperationHandler(settingName);
        return operationHandler == null ? null : operationHandler.create(testCase, settingName, args, comment);
    }

    public AModelElement<?> createExecutableRow(final TestCase testCase, final String action, final String comment,
            final List<String> args) {
        if (testCase == null) {
            return null;
        }
        final ITestCaseTableElementOperation operationHandler = getOperationHandler(ModelType.TEST_CASE_EXECUTABLE_ROW);
        return operationHandler == null ? null : operationHandler.create(testCase, action, args, comment);
    }

    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        if (modelElement != null) {
            final ITestCaseTableElementOperation operationHandler = getOperationHandler(modelElement.getModelType());
            if (operationHandler != null) {
                operationHandler.update(modelElement, index, value);
            }
        }
    }

    public void updateComment(final AModelElement<?> modelElement, final String value) {
        if (modelElement != null) {
            CommentServiceHandler.update((ICommentHolder) modelElement, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE, value);
        }
    }

    public void remove(final TestCase testCase, final AModelElement<?> modelElement) {
        if (modelElement != null) {
            final ITestCaseTableElementOperation operationHandler = getOperationHandler(modelElement.getModelType());
            if (operationHandler != null) {
                operationHandler.remove(testCase, modelElement);
            }
        }
    }

    public void insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        // morph operations enables inserting settings taken from keywords elements
        if (modelElement != null) {
            final ITestCaseTableElementOperation operationHandler = getOperationHandler(modelElement.getModelType());
            if (operationHandler != null) {
                operationHandler.insert(testCase, index, modelElement);
            }
        }
    }

    @VisibleForTesting
    ITestCaseTableElementOperation getOperationHandler(final ModelType elementModelType) {
        for (final ITestCaseTableElementOperation operation : elementOparations) {
            if (operation.isApplicable(elementModelType)) {
                return operation;
            }
        }
        return null;
    }

    private ITestCaseTableElementOperation getOperationHandler(final String settingName) {
        final RobotTokenType type = RobotTokenType.findTypeOfDeclarationForTestCaseSettingTable(settingName);
        return getOperationHandler(
                type == RobotTokenType.UNKNOWN ? RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION : type);
    }

    @VisibleForTesting
    ITestCaseTableElementOperation getOperationHandler(final IRobotTokenType type) {
        for (final ITestCaseTableElementOperation operation : elementOparations) {
            if (operation.isApplicable(type)) {
                return operation;
            }
        }
        return null;
    }
}
