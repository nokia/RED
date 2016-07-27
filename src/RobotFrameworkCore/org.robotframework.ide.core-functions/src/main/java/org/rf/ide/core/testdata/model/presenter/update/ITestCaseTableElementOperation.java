package org.rf.ide.core.testdata.model.presenter.update;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;

public interface ITestCaseTableElementOperation {

    boolean isApplicable(final ModelType elementType);

    boolean isApplicable(final IRobotTokenType elementType);

    AModelElement<?> create(final TestCase testCase, String action, final List<String> args,
            final String comment);

    void update(final AModelElement<?> modelElement, final int index, final String value);

    void remove(TestCase testCase, AModelElement<?> modelElement);

    void insert(TestCase testCase, int index, AModelElement<?> modelElement);

}
