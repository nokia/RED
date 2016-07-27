package org.rf.ide.core.testdata.model.presenter.update.testcases;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ITestCaseTableElementOperation;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class UserKeywordExecutableRowMorphOperation implements ITestCaseTableElementOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_EXECUTABLE_ROW;
    }

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return false;
    }

    @Override
    public AModelElement<?> create(final TestCase testCase, final String actionName, final List<String> args,
            final String comment) {
        throw new IllegalStateException();
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        throw new IllegalStateException();
    }

    @Override
    public void remove(final TestCase testCase, final AModelElement<?> modelElement) {
        throw new IllegalStateException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        final RobotExecutableRow<TestCase> executableRow = (RobotExecutableRow<TestCase>) modelElement;
        executableRow.setParent(testCase);

        // executable row could be taken from some keyword originally, so we need to fix types in
        // its tokens
        final RobotToken action = executableRow.getAction();
        action.getTypes().clear();
        executableRow.setAction(action);

        for (int i = 0; i < executableRow.getArguments().size(); i++) {
            final RobotToken arg = executableRow.getArguments().get(i);
            arg.getTypes().clear();
            executableRow.setArgument(i, arg);
        }

        testCase.addTestExecutionRow(executableRow, index);
    }
}
