/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug.contexts;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptor;


class ForLoop {

    private final ExecutableWithDescriptor loopHeader;

    private final List<ExecutableWithDescriptor> innerExecutables;

    ForLoop(final ExecutableWithDescriptor loopHeader, final List<ExecutableWithDescriptor> executables) {
        this.loopHeader = loopHeader;
        this.innerExecutables = executables;
    }

    RobotExecutableRow<?> getExecutable() {
        return loopHeader.getExecutable();
    }

    ForLoopDeclarationRowDescriptor<?> getDescriptor() {
        return (ForLoopDeclarationRowDescriptor<?>) loopHeader.getDescriptor();
    }

    List<ExecutableWithDescriptor> getInnerExecutables() {
        return innerExecutables;
    }

    List<IExecutableRowDescriptor<?>> getChildrenDescriptors() {
        return innerExecutables.stream().map(ExecutableWithDescriptor::getDescriptor).collect(toList());
    }
}
