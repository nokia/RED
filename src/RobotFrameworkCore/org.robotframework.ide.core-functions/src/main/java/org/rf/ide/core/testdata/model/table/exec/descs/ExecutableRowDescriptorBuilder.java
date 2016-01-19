/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IRowDescriptorBuilder.AcceptResult;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopContinueRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.SimpleRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.SimpleRowDescriptorBuilder;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.annotations.VisibleForTesting;

public class ExecutableRowDescriptorBuilder {

    private final List<IRowDescriptorBuilder> builders = new ArrayList<>();

    public ExecutableRowDescriptorBuilder() {
        builders.add(new ForLoopDeclarationRowDescriptorBuilder());
        builders.add(new ForLoopContinueRowDescriptorBuilder());
    }

    @VisibleForTesting
    public ExecutableRowDescriptorBuilder(final List<IRowDescriptorBuilder> builders) {
        this.builders.addAll(builders);
    }

    public <T> IExecutableRowDescriptor<T> buildLineDescriptor(final RobotExecutableRow<T> execRowLine) {
        IExecutableRowDescriptor<T> rowDesc = new SimpleRowDescriptor<T>(execRowLine);

        if (execRowLine.isExecutable()) {
            List<RobotToken> elementTokens = execRowLine.getElementTokens();
            if (!elementTokens.isEmpty()) {
                IRowDescriptorBuilder builderToUse = new SimpleRowDescriptorBuilder();
                AcceptResult acceptResult = new AcceptResult(true);
                for (IRowDescriptorBuilder builder : builders) {
                    acceptResult = builder.acceptable(execRowLine);
                    if (acceptResult.shouldAccept()) {
                        builderToUse = builder;
                        break;
                    }
                }

                rowDesc = builderToUse.buildDescription(execRowLine, acceptResult);
            }
        } else {
            rowDesc = new SimpleRowDescriptorBuilder().buildDescription(execRowLine, new AcceptResult(true));
        }

        return rowDesc;
    }

    public List<IRowDescriptorBuilder> getBuilders() {
        return Collections.unmodifiableList(builders);
    }
}
