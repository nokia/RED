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
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.collect.Range;

public class RedSectionProposalTest {

    @Test
    public void testProposalWithEmptySectionNameAndEmptyMatch() {
        final RedSectionProposal proposal = new RedSectionProposal("", ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEmpty();
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotCasesFileSectionImage());
        assertThat(proposal.getLabel()).isEmpty();
        assertThat(proposal.getStyledLabel().length()).isEqualTo(0);
        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void testProposalWithNonEmptyContentAndEmptyMatch() {
        final RedSectionProposal proposal = new RedSectionProposal("section", ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("section");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotCasesFileSectionImage());
        assertThat(proposal.getLabel()).isEqualTo("section");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("section");
        assertThat(proposal.getStyledLabel().getStyleRanges()).isEmpty();
        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void testProposalWithNonEmptyContentAndNonEmptyMatch() {
        final RedSectionProposal proposal = new RedSectionProposal("section",
                new ProposalMatch(Range.closedOpen(1, 3)));

        assertThat(proposal.getContent()).isEqualTo("section");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotCasesFileSectionImage());
        assertThat(proposal.getLabel()).isEqualTo("section");

        assertThat(proposal.getStyledLabel().getString()).isEqualTo("section");
        final TextStyle matchStyle = new TextStyle();
        Stylers.Common.MARKED_PREFIX_STYLER.applyStyles(matchStyle);

        final StyleRange[] ranges = proposal.getStyledLabel().getStyleRanges();
        assertThat(ranges).hasSize(1);

        assertThat(ranges[0].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(matchStyle.foreground.getRGB());
        assertThat(ranges[0].borderColor.getRGB()).isEqualTo(matchStyle.borderColor.getRGB());
        assertThat(ranges[0].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(ranges[0].strikeout).isFalse();
        assertThat(ranges[0].start).isEqualTo(1);
        assertThat(ranges[0].length).isEqualTo(2);

        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }
}
