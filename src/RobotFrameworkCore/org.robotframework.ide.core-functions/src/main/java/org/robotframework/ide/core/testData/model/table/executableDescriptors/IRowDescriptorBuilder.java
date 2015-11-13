/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors;

import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;


public interface IRowDescriptorBuilder {

    <T> boolean acceptable(final RobotExecutableRow<T> execRowLine);


    <T> IExecutableRowDescriptor<T> buildDescription(
            final RobotExecutableRow<T> execRowLine);
}
