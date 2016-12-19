/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.EnumSet;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;

public class RedNewVariableProposalTest {

    @Test
    public void itIsNotPossibleToCreateProposalForModelTypeDifferentThanLibraryOrResourceImport() {
        for (final VariableType type : EnumSet
                .complementOf(EnumSet.of(VariableType.SCALAR, VariableType.LIST, VariableType.DICTIONARY))) {
            try {
                new RedNewVariableProposal("content", type, new ArrayList<String>(), null, "label", "desc");
                fail();
            } catch (final IllegalArgumentException e) {
                continue;
            }
            fail();
        }
    }

}
