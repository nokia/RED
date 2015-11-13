/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.impl;

import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.IExecutableRowDescriptor;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.IRowDescriptorBuilder;


public class ForLoopDeclarationRowDescriptorBuilder implements
        IRowDescriptorBuilder {

    @Override
    public <T> boolean acceptable(RobotExecutableRow<T> execRowLine) {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public <T> IExecutableRowDescriptor<T> buildDescription(
            RobotExecutableRow<T> execRowLine) {
        // TODO Auto-generated method stub
        return null;
    }

}
