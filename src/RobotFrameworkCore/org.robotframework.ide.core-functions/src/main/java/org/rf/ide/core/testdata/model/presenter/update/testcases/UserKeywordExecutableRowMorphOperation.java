package org.rf.ide.core.testdata.model.presenter.update.testcases;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class UserKeywordExecutableRowMorphOperation extends UserKeywordElementMorphOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_EXECUTABLE_ROW;
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
