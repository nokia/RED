/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.impl.SimpleRowDescriptor;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.impl.SimpleRowDescriptorBuilder;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;

import com.google.common.annotations.VisibleForTesting;


public class ExecutableRowDescriptorBuilder {

    private final List<IRowDescriptorBuilder> builders = new LinkedList<>();


    public ExecutableRowDescriptorBuilder() {

    }


    @VisibleForTesting
    public ExecutableRowDescriptorBuilder(
            final List<IRowDescriptorBuilder> builders) {
        this.builders.addAll(builders);
    }


    public <T> IExecutableRowDescriptor<T> buildLineDescriptor(
            final RobotExecutableRow<T> execRowLine) {
        IExecutableRowDescriptor<T> rowDesc = new SimpleRowDescriptor<T>(
                execRowLine);

        if (execRowLine.isExecutable()) {
            List<RobotToken> elementTokens = execRowLine.getElementTokens();
            if (!elementTokens.isEmpty()) {
                IRowDescriptorBuilder builderToUse = new SimpleRowDescriptorBuilder();
                for (IRowDescriptorBuilder builder : builders) {
                    if (builder.acceptable(execRowLine)) {
                        builderToUse = builder;
                        break;
                    }
                }

                rowDesc = builderToUse.buildDescription(execRowLine);
            }
        } else {
            rowDesc = new SimpleRowDescriptorBuilder()
                    .buildDescription(execRowLine);
        }

        return rowDesc;
    }


    public List<IRowDescriptorBuilder> getBuilders() {
        return Collections.unmodifiableList(builders);
    }
}
