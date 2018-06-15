/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs;

import java.util.stream.Stream;

import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopContinueRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.SimpleRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.SimpleRowDescriptorBuilder;

public class ExecutableRowDescriptorBuilder {

    public <T> IExecutableRowDescriptor<T> buildLineDescriptor(final RobotExecutableRow<T> execRowLine) {
        if (execRowLine.isExecutable()) {
            if (execRowLine.getElementTokens().isEmpty()) {
                return new SimpleRowDescriptor<>(execRowLine);
            }
            return getBuilders().filter(builder -> builder.isAcceptable(execRowLine))
                    .findFirst()
                    .get()
                    .buildDescription(execRowLine);
        } else {
            return new SimpleRowDescriptorBuilder().buildDescription(execRowLine);
        }
    }

    private Stream<IRowDescriptorBuilder> getBuilders() {
        return Stream.of(
                new ForLoopDeclarationRowDescriptorBuilder(), 
                new ForLoopContinueRowDescriptorBuilder(),
                new SimpleRowDescriptorBuilder());
    }
}
