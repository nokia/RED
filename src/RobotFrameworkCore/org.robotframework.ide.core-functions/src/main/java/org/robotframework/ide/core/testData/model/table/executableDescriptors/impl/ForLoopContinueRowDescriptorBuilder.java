/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.impl;

import java.util.List;

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
    public <T> AcceptResult acceptable(final RobotExecutableRow<T> execRowLine) {
        AcceptResult result = new AcceptResult(false);
        String text = execRowLine.getAction().getText().toString();
        if (text != null && !text.trim().isEmpty()) {
            String trimmed = text.trim();
            if (RobotTokenType.FOR_CONTINUE_TOKEN.getRepresentation().get(0)
                    .equalsIgnoreCase(trimmed)) {
                int forLoopDeclarationLine = getForLoopDeclarationLine(execRowLine);
                result = new AcceptResultWithParameters(
                        forLoopDeclarationLine >= 0, forLoopDeclarationLine);
            }
        }

        return result;
    }

    public class AcceptResultWithParameters extends AcceptResult {

        private final int forLoopPosition;


        public AcceptResultWithParameters(boolean shouldAccept,
                final int forLoopPosition) {
            super(shouldAccept);
            this.forLoopPosition = forLoopPosition;
        }


        public int getForLoopPosition() {
            return forLoopPosition;
        }
    }


    @Override
    public <T> IExecutableRowDescriptor<T> buildDescription(
            final RobotExecutableRow<T> execRowLine,
            final AcceptResult acceptResult) {
        AcceptResultWithParameters acceptResultWithParams = (AcceptResultWithParameters) acceptResult;

        return null;
    }


    private <T> int getForLoopDeclarationLine(
            final RobotExecutableRow<T> execRowLine) {
        int forLine = -1;

        @SuppressWarnings("unchecked")
        IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> keywordOrTest = (IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>>) execRowLine
                .getParent();
        List<RobotExecutableRow<AModelElement<? extends ARobotSectionTable>>> executionContext = keywordOrTest
                .getExecutionContext();

        return forLine;
    }
}
