/*
 * Copyright 2016 Nokia Solutions and Networks
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

public class LibraryAliasReservedWordProposalTest {

    @Test
    public void testProposalWithEmptyMatch() {
        final LibraryAliasReservedWordProposal proposal = new LibraryAliasReservedWordProposal(ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("WITH NAME");
        assertThat(proposal.getArguments()).containsExactly("alias");
        assertThat(proposal.getImage()).isNull();
        assertThat(proposal.getLabel()).isEqualTo("WITH NAME");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("WITH NAME");
        assertThat(proposal.getStyledLabel().getStyleRanges()).isEmpty();
        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void testProposalWithNonEmptyMatch() {
        final LibraryAliasReservedWordProposal proposal = new LibraryAliasReservedWordProposal(
                new ProposalMatch(Range.closedOpen(1, 3), Range.closedOpen(4, 7)));

        assertThat(proposal.getContent()).isEqualTo("WITH NAME");
        assertThat(proposal.getArguments()).containsExactly("alias");
        assertThat(proposal.getImage()).isNull();
        assertThat(proposal.getLabel()).isEqualTo("WITH NAME");

        assertThat(proposal.getStyledLabel().getString()).isEqualTo("WITH NAME");
        final TextStyle matchStyle = new TextStyle();
        Stylers.Common.MATCH_DECORATION_STYLER.applyStyles(matchStyle);

        final StyleRange[] ranges = proposal.getStyledLabel().getStyleRanges();
        assertThat(ranges).hasSize(2);

        assertThat(ranges[0].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(matchStyle.foreground.getRGB());
        assertThat(ranges[0].borderColor.getRGB()).isEqualTo(matchStyle.borderColor.getRGB());
        assertThat(ranges[0].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(ranges[0].strikeout).isFalse();
        assertThat(ranges[0].start).isEqualTo(1);
        assertThat(ranges[0].length).isEqualTo(2);

        assertThat(ranges[1].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(ranges[1].foreground.getRGB()).isEqualTo(matchStyle.foreground.getRGB());
        assertThat(ranges[1].borderColor.getRGB()).isEqualTo(matchStyle.borderColor.getRGB());
        assertThat(ranges[1].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(ranges[1].strikeout).isFalse();
        assertThat(ranges[1].start).isEqualTo(4);
        assertThat(ranges[1].length).isEqualTo(3);

        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

}
