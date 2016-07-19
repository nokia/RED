package org.rf.ide.core.testdata.model.presenter.update;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;

public interface ITestCaseTableElementOperation {

    boolean isApplicable(final ModelType elementType);

    boolean isApplicable(final IRobotTokenType elementType);

    AModelElement<TestCase> create(final TestCase testCase, final List<String> args, final String comment);

    void update(final AModelElement<TestCase> modelElement, final int index, final String value);

}
