/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.impl;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.ForDescriptorInfo;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesAnalyzer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class ForLoopDeclarationRowDescriptorBuilder implements IRowDescriptorBuilder {

    private final RobotVersion version;

    public ForLoopDeclarationRowDescriptorBuilder(final RobotVersion version) {
        this.version = version;
    }

    @Override
    public <T> boolean isAcceptable(final RobotExecutableRow<T> execRowLine) {
        return execRowLine.getAction().getTypes().contains(RobotTokenType.FOR_TOKEN)
                && execRowLine.getParent() instanceof IExecutableStepsHolder<?>;
    }

    @Override
    public <T> IExecutableRowDescriptor<T> buildDescription(final RobotExecutableRow<T> execRowLine) {
        final ForLoopDeclarationRowDescriptor<T> loopDescriptor = new ForLoopDeclarationRowDescriptor<>(execRowLine);

        boolean foundFor = false;
        boolean foundIn = false;
        boolean hasElementsToIterate = false;
        for (final RobotToken elem : execRowLine.getElementTokens()) {
            if (elem.getTypes().contains(RobotTokenType.COMMENT)) {
                break;
            }

            final List<VariableUse> varUses = new ArrayList<>();
            VariablesAnalyzer.analyzer(version).visitVariables(elem, varUses::add);

            if (foundFor && foundIn) {
                loopDescriptor.addUsedVariables(varUses);
                hasElementsToIterate = true;

            } else if (foundFor && !foundIn && ForDescriptorInfo.isInToken(elem)) {
                loopDescriptor.setInAction(elem.copy());
                foundIn = true;

            } else if (foundFor && !foundIn) {
                loopDescriptor.addCreatedVariables(varUses);

                if (varUses.size() != 1 || !varUses.get(0).isPlainVariable()) {
                    loopDescriptor.addMessage(BuildMessage.createErrorMessage(
                            "Invalid FOR loop variable \'" + elem.getText() + "\'", elem.getFileRegion()));
                }

            } else if (elem.getTypes().contains(RobotTokenType.FOR_TOKEN)) {
                loopDescriptor.setAction(elem.copy());
                foundFor = true;

            } else {
                throw new IllegalStateException("Internal problem - FOR should be the first token.");
            }
        }

        if (!foundIn || !hasElementsToIterate) {
            loopDescriptor.addMessage(BuildMessage.createErrorMessage("Invalid FOR loop - missing values to iterate",
                    loopDescriptor.getAction().getFileRegion()));
        }
        return loopDescriptor;
    }
}
