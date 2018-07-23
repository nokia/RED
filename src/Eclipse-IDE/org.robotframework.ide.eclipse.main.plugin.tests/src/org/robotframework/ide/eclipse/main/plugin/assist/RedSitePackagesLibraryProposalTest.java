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

public class RedSitePackagesLibraryProposalTest {

    @Test
    public void testSitePackagesProposalWithEmptyContentAndEmptyMatch() {
        final RedSitePackagesLibraryProposal proposal = new RedSitePackagesLibraryProposal("", false,
                ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.isImported()).isFalse();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getPythonSitePackagesLibraryImage());
        assertThat(proposal.getLabel()).isEmpty();
        assertThat(proposal.getStyledLabel().length()).isEqualTo(0);
        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void testSitePackagesProposalWithNonEmptyContentAndEmptyMatch() {
        final RedSitePackagesLibraryProposal proposal = new RedSitePackagesLibraryProposal("libName", false,
                ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("libName");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.isImported()).isFalse();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getPythonSitePackagesLibraryImage());
        assertThat(proposal.getLabel()).isEqualTo("libName");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("libName");
        assertThat(proposal.getStyledLabel().getStyleRanges()).isEmpty();
        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void testSitePackagesProposalWithNonEmptyContentAndEmptyMatchAlreadyImported() {
        final RedSitePackagesLibraryProposal proposal = new RedSitePackagesLibraryProposal("libName", true,
                ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("libName");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.isImported()).isTrue();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getLibraryImage());
        assertThat(proposal.getLabel()).isEqualTo("libName");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("libName (already imported)");

        final TextStyle decorationStyle = new TextStyle();
        Stylers.Common.ECLIPSE_DECORATION_STYLER.applyStyles(decorationStyle);

        final StyleRange[] ranges = proposal.getStyledLabel().getStyleRanges();
        assertThat(ranges).hasSize(1);
        assertThat(ranges[0].background).isNull();
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(decorationStyle.foreground.getRGB());
        assertThat(ranges[0].borderColor).isNull();
        assertThat(ranges[0].borderStyle).isEqualTo(decorationStyle.borderStyle);
        assertThat(ranges[0].strikeout).isFalse();
        assertThat(ranges[0].start).isEqualTo(7);
        assertThat(ranges[0].length).isEqualTo(19);

        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void testSitePackagesProposalWithNonEmptyContentAndNonEmptyMatch() {
        final RedSitePackagesLibraryProposal proposal = new RedSitePackagesLibraryProposal("libName", false,
                new ProposalMatch(Range.closedOpen(1, 5)));

        assertThat(proposal.getContent()).isEqualTo("libName");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.isImported()).isFalse();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getPythonSitePackagesLibraryImage());
        assertThat(proposal.getLabel()).isEqualTo("libName");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("libName");

        final TextStyle decorationStyle = new TextStyle();
        Stylers.Common.ECLIPSE_DECORATION_STYLER.applyStyles(decorationStyle);
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
        assertThat(ranges[0].length).isEqualTo(4);

        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void testSitePackagesProposalWithNonEmptyContentAndNonEmptyMatchAlreadyImported() {
        final RedSitePackagesLibraryProposal proposal = new RedSitePackagesLibraryProposal("libName", true,
                new ProposalMatch(Range.closedOpen(1, 5)));

        assertThat(proposal.getContent()).isEqualTo("libName");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.isImported()).isTrue();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getLibraryImage());
        assertThat(proposal.getLabel()).isEqualTo("libName");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("libName (already imported)");

        final TextStyle decorationStyle = new TextStyle();
        Stylers.Common.ECLIPSE_DECORATION_STYLER.applyStyles(decorationStyle);
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
        assertThat(ranges[0].length).isEqualTo(4);

        assertThat(ranges[1].background).isNull();
        assertThat(ranges[1].foreground.getRGB()).isEqualTo(decorationStyle.foreground.getRGB());
        assertThat(ranges[1].borderColor).isNull();
        assertThat(ranges[1].borderStyle).isEqualTo(decorationStyle.borderStyle);
        assertThat(ranges[1].strikeout).isFalse();
        assertThat(ranges[1].start).isEqualTo(7);
        assertThat(ranges[1].length).isEqualTo(19);

        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }
}