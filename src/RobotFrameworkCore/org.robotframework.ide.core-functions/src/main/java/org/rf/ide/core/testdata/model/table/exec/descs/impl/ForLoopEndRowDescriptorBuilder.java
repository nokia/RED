package org.rf.ide.core.testdata.model.table.exec.descs.impl;

import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IRowDescriptorBuilder;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class ForLoopEndRowDescriptorBuilder implements IRowDescriptorBuilder {

    @Override
    public <T> boolean isAcceptable(final RobotExecutableRow<T> execRowLine) {
        return execRowLine.getParent() instanceof IExecutableStepsHolder<?>
                && execRowLine.getAction().getTypes().contains(RobotTokenType.FOR_END_TOKEN)
                && execRowLine.getArguments().isEmpty();
    }

    @Override
    public <T> IExecutableRowDescriptor<T> buildDescription(final RobotExecutableRow<T> execRowLine) {
        return new ForLoopEndRowDescriptor<>(execRowLine);
    }
}
