/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;

public class RedNewVariableProposals {

    public List<? extends AssistProposal> getNewVariableProposals() {
        final List<AssistProposal> proposals = new ArrayList<>();

        for (final VariableType type : newArrayList(VariableType.SCALAR, VariableType.LIST, VariableType.DICTIONARY)) {
            proposals.add(AssistProposals.createNewVariableProposal(type));
        }
        return proposals;
    }
}
