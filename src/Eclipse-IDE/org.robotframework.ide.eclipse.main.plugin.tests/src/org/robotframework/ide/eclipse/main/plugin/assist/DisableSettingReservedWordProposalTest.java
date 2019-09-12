/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.Test;
import org.robotframework.red.jface.viewers.Stylers;

import com.google.common.collect.Range;

public class DisableSettingReservedWordProposalTest {

    @Test
    public void testProposalWithEmptyMatch() {
        final DisableSettingReservedWordProposal proposal = new DisableSettingReservedWordProposal(ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("NONE");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isNull();
        assertThat(proposal.getLabel()).isEqualTo("NONE");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("NONE");
        assertThat(proposal.getStyledLabel().getStyleRanges()).isEmpty();
        assertThat(proposal.isDocumented()).isTrue();
        assertThat(proposal.getDescription()).isNotEmpty();
    }

    @Test
    public void testProposalWithNonEmptyMatch() {
        final DisableSettingReservedWordProposal proposal = new DisableSettingReservedWordProposal(new ProposalMatch(Range.closedOpen(1, 3)));

        assertThat(proposal.getContent()).isEqualTo("NONE");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isNull();
        assertThat(proposal.getLabel()).isEqualTo("NONE");

        assertThat(proposal.getStyledLabel().getString()).isEqualTo("NONE");
        final TextStyle matchStyle = new TextStyle();
        Stylers.Common.MATCH_DECORATION_STYLER.applyStyles(matchStyle);

        final StyleRange[] ranges = proposal.getStyledLabel().getStyleRanges();
        assertThat(ranges).hasSize(1);

        assertThat(ranges[0].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(matchStyle.foreground.getRGB());
        assertThat(ranges[0].borderColor.getRGB()).isEqualTo(matchStyle.borderColor.getRGB());
        assertThat(ranges[0].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(ranges[0].strikeout).isFalse();
        assertThat(ranges[0].start).isEqualTo(1);
        assertThat(ranges[0].length).isEqualTo(2);

        assertThat(proposal.isDocumented()).isTrue();
        assertThat(proposal.getDescription()).isNotEmpty();
    }

}
