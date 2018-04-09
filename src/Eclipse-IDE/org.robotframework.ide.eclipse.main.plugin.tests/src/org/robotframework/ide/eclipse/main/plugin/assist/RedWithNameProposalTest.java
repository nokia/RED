/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.Test;

import com.google.common.collect.Range;

public class RedWithNameProposalTest {

    @Test
    public void testProposalWithEmptyContentAndEmptyMatch() {
        final RedWithNameProposal proposal = new RedWithNameProposal("", ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEmpty();
        assertThat(proposal.getArguments()).containsExactly("alias");
        assertThat(proposal.getImage()).isNull();
        assertThat(proposal.getLabel()).isEmpty();
        assertThat(proposal.getStyledLabel().length()).isEqualTo(0);
        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void testProposalWithNonEmptyContentAndEmptyMatch() {
        final RedWithNameProposal proposal = new RedWithNameProposal("content", ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("content");
        assertThat(proposal.getArguments()).containsExactly("alias");
        assertThat(proposal.getImage()).isNull();
        assertThat(proposal.getLabel()).isEqualTo("content");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("content");
        assertThat(proposal.getStyledLabel().getStyleRanges()).isEmpty();
        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void testProposalWithNonEmptyContentAndNonEmptyMatch() {
        final RedWithNameProposal proposal = new RedWithNameProposal("content",
                new ProposalMatch(Range.closedOpen(1, 3), Range.closedOpen(4, 7)));

        assertThat(proposal.getContent()).isEqualTo("content");
        assertThat(proposal.getArguments()).containsExactly("alias");
        assertThat(proposal.getImage()).isNull();
        assertThat(proposal.getLabel()).isEqualTo("content");

        assertThat(proposal.getStyledLabel().getString()).isEqualTo("content");
        final TextStyle matchStyle = new TextStyle();
        Stylers.Common.MARKED_PREFIX_STYLER.applyStyles(matchStyle);

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
