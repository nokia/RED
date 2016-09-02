package org.rf.ide.core.testdata.model.presenter.update.testcases;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.presenter.update.ITestCaseTableElementOperation;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;

abstract class UserKeywordElementMorphOperation implements ITestCaseTableElementOperation {

    @Override
    public final boolean isApplicable(final IRobotTokenType elementType) {
        return false;
    }

    @Override
    public final AModelElement<?> create(final TestCase testCase, final String action, final List<String> args,
            final String comment) {
        throw new IllegalStateException();
    }

    @Override
    public final void update(final AModelElement<?> modelElement, final int index, final String value) {
        throw new IllegalStateException();
    }

    @Override
    public final void remove(final TestCase testCase, final AModelElement<?> modelElement) {
        throw new IllegalStateException();
    }
}
