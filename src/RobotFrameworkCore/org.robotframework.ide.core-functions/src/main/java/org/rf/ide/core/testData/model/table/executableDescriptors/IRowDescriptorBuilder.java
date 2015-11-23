/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testData.model.table.executableDescriptors;

import org.rf.ide.core.testData.model.table.RobotExecutableRow;


public interface IRowDescriptorBuilder {

    <T> AcceptResult acceptable(final RobotExecutableRow<T> execRowLine);


    <T> IExecutableRowDescriptor<T> buildDescription(
            final RobotExecutableRow<T> execRowLine,
            final AcceptResult acceptResult);

    public class AcceptResult {

        private final boolean shouldAccept;


        public AcceptResult(final boolean shouldAccept) {
            this.shouldAccept = shouldAccept;
        }


        public boolean shouldAccept() {
            return shouldAccept;
        }
    }
}
