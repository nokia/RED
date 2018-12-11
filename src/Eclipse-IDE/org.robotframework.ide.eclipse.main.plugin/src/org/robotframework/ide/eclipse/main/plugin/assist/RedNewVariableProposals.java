/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;

public class RedNewVariableProposals {

    public List<RedNewVariableProposal> getNewVariableProposals() {
        return Stream.of(VariableType.SCALAR, VariableType.LIST, VariableType.DICTIONARY)
                .map(AssistProposals::createNewVariableProposal)
                .collect(Collectors.toList());
    }
}
