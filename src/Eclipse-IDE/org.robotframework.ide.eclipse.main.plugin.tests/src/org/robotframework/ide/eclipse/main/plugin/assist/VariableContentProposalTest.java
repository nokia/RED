/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal.VariableOrigin;

public class VariableContentProposalTest {

    @Test
    public void checkPropertiesExposedByContentProposalBean_1() {
        final VariableContentProposal proposal = new VariableContentProposal(createProposalToWrap(), "");

        assertThat(proposal.getContent()).isEqualTo("${name}");
        assertThat(proposal.getCursorPosition()).isEqualTo(7);
        assertThat(proposal.getLabel()).isEqualTo("${name}");
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotScalarVariableImage());
        assertThat(proposal.getLabelDecoration()).isEmpty();
        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getMatchingPrefix()).isEmpty();
        final String description = proposal.getDescription();

        assertThat(description).contains("&lt;source&gt;", "&amp;{value}");
    }

    private RedVariableProposal createProposalToWrap() {
        return new RedVariableProposal("${name}", "<source>", "&{value}", "comment", VariableOrigin.BUILTIN);
    }
}
