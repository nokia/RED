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
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals.SettingTarget;

import com.google.common.collect.Range;

public class RedSettingProposalTest {

    @Test
    public void testGeneralSettingProposalWithEmptyMatch() {
        final RedSettingProposal proposal = new RedSettingProposal("Documentation", SettingTarget.GENERAL,
                ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("Documentation");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotSettingImage());
        assertThat(proposal.getLabel()).isEqualTo("Documentation");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("Documentation");
        assertThat(proposal.getStyledLabel().getStyleRanges()).hasSize(0);
        assertThat(proposal.isDocumented()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Documentation of current suite");
    }

    @Test
    public void testCaseSettingProposalWithEmptyMatch() {
        final RedSettingProposal proposal = new RedSettingProposal("[Documentation]", SettingTarget.TEST_CASE,
                ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("[Documentation]");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotSettingImage());
        assertThat(proposal.getLabel()).isEqualTo("[Documentation]");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("[Documentation]");
        assertThat(proposal.getStyledLabel().getStyleRanges()).hasSize(0);
        assertThat(proposal.isDocumented()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Documentation of current test case");
    }

    @Test
    public void testKeywordSettingProposalWithEmptyMatch() {
        final RedSettingProposal proposal = new RedSettingProposal("[Documentation]", SettingTarget.KEYWORD,
                ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("[Documentation]");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotSettingImage());
        assertThat(proposal.getLabel()).isEqualTo("[Documentation]");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("[Documentation]");
        assertThat(proposal.getStyledLabel().getStyleRanges()).hasSize(0);
        assertThat(proposal.isDocumented()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Documentation of current keyword");
    }

    @Test
    public void testProposalWithNonEmptyMatch() {
        final RedSettingProposal proposal = new RedSettingProposal("Documentation", SettingTarget.GENERAL,
                new ProposalMatch(Range.closedOpen(1, 3), Range.closedOpen(4, 7)));

        assertThat(proposal.getContent()).isEqualTo("Documentation");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotSettingImage());
        assertThat(proposal.getLabel()).isEqualTo("Documentation");

        assertThat(proposal.getStyledLabel().getString()).isEqualTo("Documentation");
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

        assertThat(proposal.isDocumented()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Documentation of current suite");
    }
}
