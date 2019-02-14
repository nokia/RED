/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.RowType;

public class ExecutablesCompiler {

    static List<ExecutableWithDescriptor> compileExecutables(final List<? extends RobotExecutableRow<?>> executables,
            final String template) {

        final List<ExecutableWithDescriptor> elements = new ArrayList<>();
        final List<ExecutableWithDescriptor> loopElements = new ArrayList<>();

        ExecutableWithDescriptor loopHeader = null;
        for (final RobotExecutableRow<?> executableRow : executables) {
            final IExecutableRowDescriptor<?> descriptor = executableRow.buildLineDescription();

            if (descriptor.getRowType() == RowType.FOR) {
                loopHeader = new ExecutableWithDescriptor(executableRow, descriptor, template);

            } else if (descriptor.getRowType() == RowType.FOR_CONTINUE && loopHeader != null) {
                loopElements.add(new ExecutableWithDescriptor(executableRow, descriptor, template));

            } else {
                if (loopHeader != null) {
                    elements.add(new ExecutableWithDescriptor(new ForLoop(loopHeader, newArrayList(loopElements)),
                            template));
                }
                if (descriptor.getRowType() != RowType.FOR_END) {
                    elements.add(new ExecutableWithDescriptor(executableRow, descriptor, template));
                }
                loopElements.clear();
                loopHeader = null;
            }
        }
        if (loopHeader != null) {
            elements.add(new ExecutableWithDescriptor(new ForLoop(loopHeader, newArrayList(loopElements)), template));
        }
        return elements;
    }
}
