/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.impl;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesAnalyzer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class SimpleRowDescriptorBuilder implements IRowDescriptorBuilder {

    private final RobotVersion version;

    public SimpleRowDescriptorBuilder(final RobotVersion version) {
        this.version = version;
    }

    @Override
    public <T> boolean isAcceptable(final RobotExecutableRow<T> execRowLine) {
        return true;
    }

    @Override
    public <T> IExecutableRowDescriptor<T> buildDescription(final RobotExecutableRow<T> execRowLine) {
        final SimpleRowDescriptor<T> simpleDesc = new SimpleRowDescriptor<>(execRowLine);

        final List<VariableUse> createdVariables = new ArrayList<>();
        final List<VariableUse> usedVariables = new ArrayList<>();

        boolean foundAction = false;
        for (final RobotToken elem : execRowLine.getElementTokens()) {
            if (elem.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                    || elem.getTypes().contains(RobotTokenType.COMMENT_CONTINUE)) {
                break;
            }

            final List<VariableUse> varUses = VariablesAnalyzer.analyzer(version)
                    .getVariablesUses(elem, simpleDesc::addMessage)
                    .collect(toList());

            // value is a keyword if is on the first place and is not just a variable or
            // variable with equal sign. Keyword can be defined as a ${var}=, however must
            // be called with a plain text in the place of an embedded variable

            if (foundAction) {
                usedVariables.addAll(varUses);
                simpleDesc.addKeywordArgument(elem.copy());

            } else if (varUses.size() == 1 && varUses.get(0).isPlainVariableAssign()) {
                createdVariables.add(varUses.get(0));

            } else {
                simpleDesc.setAction(elem.copy());
                usedVariables.addAll(varUses);
                foundAction = true;
            }
        }

        if (foundAction) {
            simpleDesc.addCreatedVariables(createdVariables);
        } else {
            simpleDesc.addUsedVariables(createdVariables);
        }
        simpleDesc.addUsedVariables(usedVariables);

        return simpleDesc;
    }
}
