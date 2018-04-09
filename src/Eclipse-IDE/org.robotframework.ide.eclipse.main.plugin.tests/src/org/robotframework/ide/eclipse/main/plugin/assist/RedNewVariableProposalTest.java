/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    public void testScalarProposal() {
        final RedNewVariableProposal proposal = new RedNewVariableProposal("${scalar}", VariableType.SCALAR,
                new ArrayList<String>(), null, "lbl", "desc");

        assertThat(proposal.getType()).isEqualTo(VariableType.SCALAR);
        assertThat(proposal.getContent()).isEqualTo("${scalar}");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isNull();
        assertThat(proposal.getLabel()).isEqualTo("lbl");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("lbl");
        assertThat(proposal.isDocumented()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("desc");
    }

    @Test
    public void testListProposal() {
        final RedNewVariableProposal proposal = new RedNewVariableProposal("@{list}", VariableType.LIST,
                new ArrayList<String>(), null, "lbl", "desc");

        assertThat(proposal.getType()).isEqualTo(VariableType.LIST);
        assertThat(proposal.getContent()).isEqualTo("@{list}");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isNull();
        assertThat(proposal.getLabel()).isEqualTo("lbl");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("lbl");
        assertThat(proposal.isDocumented()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("desc");
    }

    @Test
    public void testDictionaryProposal() {
        final RedNewVariableProposal proposal = new RedNewVariableProposal("&{dict}", VariableType.DICTIONARY,
                new ArrayList<String>(), null, "lbl", "desc");

        assertThat(proposal.getType()).isEqualTo(VariableType.DICTIONARY);
        assertThat(proposal.getContent()).isEqualTo("&{dict}");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isNull();
        assertThat(proposal.getLabel()).isEqualTo("lbl");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("lbl");
        assertThat(proposal.isDocumented()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("desc");
    }
}
