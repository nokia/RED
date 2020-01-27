/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.impl;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
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
            if (elem.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                    || elem.getTypes().contains(RobotTokenType.COMMENT_CONTINUE)) {
                break;
            }

            final List<VariableUse> varUses = VariablesAnalyzer.analyzer(version)
                    .getVariablesUses(elem, loopDescriptor::addMessage)
                    .collect(toList());

            if (foundFor && foundIn) {
                loopDescriptor.addUsedVariables(varUses);
                hasElementsToIterate = true;

            } else if (foundFor && !foundIn && ForDescriptorInfo.isInToken(elem)) {
                loopDescriptor.setInAction(elem.copy());
                foundIn = true;

            } else if (foundFor && !foundIn) {
                loopDescriptor.addCreatedVariables(varUses);

                if (varUses.size() != 1 || !varUses.get(0).isPlainVariable()) {
                    final FilePosition startFilePosition = elem.getFilePosition();
                    final FilePosition end = new FilePosition(startFilePosition.getLine(), elem.getEndColumn(),
                            elem.getStartOffset() + elem.getText().length());
                    final BuildMessage errorMessage = BuildMessage.createErrorMessage(
                            "Invalid FOR loop variable \'" + elem.getText() + "\'",
                            new FileRegion(startFilePosition, end));
                    loopDescriptor.addMessage(errorMessage);
                }

            } else if (elem.getTypes().contains(RobotTokenType.FOR_TOKEN)) {
                loopDescriptor.setAction(elem.copy());
                foundFor = true;

            } else {
                throw new IllegalStateException("Internal problem - FOR should be the first token.");
            }
        }

        if (!foundIn || !hasElementsToIterate) {
            final RobotToken forToken = loopDescriptor.getAction();
            final FilePosition startFilePosition = forToken.getFilePosition();
            final FilePosition endFilePosition = new FilePosition(startFilePosition.getLine(), forToken.getEndColumn(),
                    forToken.getStartOffset() + forToken.getText().length());
            final BuildMessage errorMessage = BuildMessage.createErrorMessage(
                    "Invalid FOR loop - missing values to iterate", new FileRegion(startFilePosition, endFilePosition));
            loopDescriptor.addMessage(errorMessage);
        }

        return loopDescriptor;
    }
}
