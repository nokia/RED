/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.impl;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.IExecutableStepsHolder;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.IExecutableRowDescriptor;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.IRowDescriptorBuilder;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class ForLoopContinueRowDescriptorBuilder implements
        IRowDescriptorBuilder {

    @Override
    public <T> boolean acceptable(final RobotExecutableRow<T> execRowLine) {
        boolean result = false;
        String text = execRowLine.getAction().getText().toString();
        if (text != null && !text.trim().isEmpty()) {
            String trimmed = text.trim();
            if (RobotTokenType.FOR_CONTINUE_TOKEN.getRepresentation().get(0)
                    .equalsIgnoreCase(trimmed)) {
                IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> keywordOrTest = (IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>>) execRowLine
                        .getParent();
                System.out.println(keywordOrTest);
            }
        }

        return result;
    }


    @Override
    public <T> IExecutableRowDescriptor<T> buildDescription(
            final RobotExecutableRow<T> execRowLine) {
        // TODO Auto-generated method stub
        return null;
    }

}
