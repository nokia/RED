/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.assist.ProposalMatch;
import org.robotframework.red.jface.viewers.Stylers;

import com.google.common.collect.Range;

public class RedTemplateProposalTest {

    @Test
    public void whenTemplateIsNotAutoInsertable_isAutoInsertableReturnsFalse() throws Exception {
        final Template template = new Template("", "", "", "", false);
        final Image image = RedImages.getTemplateImage().createImage();
        final ProposalMatch match = new ProposalMatch(Range.closedOpen(0, 1));
        final RedTemplateProposal proposal = new RedTemplateProposal(template, mock(TemplateContext.class),
                mock(IRegion.class), image, match);

        assertThat(proposal.isAutoInsertable()).isFalse();
    }

    @Test
    public void whenTemplateIsAutoInsertable_isAutoInsertableReturnsTrue() throws Exception {
        final Template template = new Template("", "", "", "", true);
        final Image image = RedImages.getTemplateImage().createImage();
        final ProposalMatch match = new ProposalMatch(Range.closedOpen(0, 1));
        final RedTemplateProposal proposal = new RedTemplateProposal(template, mock(TemplateContext.class),
                mock(IRegion.class), image, match);

        assertThat(proposal.isAutoInsertable()).isTrue();
    }

    @Test
    public void proposalIsDisplayedCorrectly_whenMatcherIsEmpty() throws Exception {
        final Template template = new Template("Some Name", "Proposal description text", "", "", true);
        final Image image = RedImages.getTemplateImage().createImage();
        final ProposalMatch match = new ProposalMatch();
        final RedTemplateProposal proposal = new RedTemplateProposal(template, mock(TemplateContext.class),
                mock(IRegion.class), image, match);

        assertThat(proposal.getDisplayString()).isEqualTo("Some Name - Proposal description text");

        final StyleRange[] ranges = proposal.getStyledDisplayString().getStyleRanges();
        assertThat(ranges).hasSize(1);
        assertThat(ranges[0].start).isEqualTo(10);
        assertThat(ranges[0].length).isEqualTo(27);
        assertThat(ranges[0].foreground).isEqualTo(RedTheme.Colors.getEclipseDecorationColor());
        assertThat(ranges[0].background).isNull();
        assertThat(ranges[0].borderColor).isNull();
    }

    @Test
    public void proposalIsDisplayedCorrectly_whenMatcherContainsSingleMatch() throws Exception {
        final Template template = new Template("Some Name", "Proposal description text", "", "", true);
        final Image image = RedImages.getTemplateImage().createImage();
        final ProposalMatch match = new ProposalMatch(Range.closedOpen(1, 4));
        final RedTemplateProposal proposal = new RedTemplateProposal(template, mock(TemplateContext.class),
                mock(IRegion.class), image, match);

        assertThat(proposal.getDisplayString()).isEqualTo("Some Name - Proposal description text");

        final TextStyle matchStyle = new TextStyle();
        Stylers.Common.MATCH_DECORATION_STYLER.applyStyles(matchStyle);

        final StyleRange[] ranges = proposal.getStyledDisplayString().getStyleRanges();
        assertThat(ranges).hasSize(2);
        assertThat(ranges[0].start).isEqualTo(1);
        assertThat(ranges[0].length).isEqualTo(3);
        assertThat(ranges[0].foreground).isEqualTo(matchStyle.foreground);
        assertThat(ranges[0].background).isEqualTo(matchStyle.background);
        assertThat(ranges[0].borderColor).isEqualTo(matchStyle.borderColor);
        assertThat(ranges[1].start).isEqualTo(10);
        assertThat(ranges[1].length).isEqualTo(27);
        assertThat(ranges[1].foreground).isEqualTo(RedTheme.Colors.getEclipseDecorationColor());
        assertThat(ranges[1].background).isNull();
        assertThat(ranges[1].borderColor).isNull();
    }

    @Test
    public void proposalIsDisplayedCorrectly_whenMatcherContainsMultipleMatches() throws Exception {
        final Template template = new Template("Some Name", "Proposal description text", "", "", true);
        final Image image = RedImages.getTemplateImage().createImage();
        final ProposalMatch match = new ProposalMatch(Range.closedOpen(0, 2), Range.closedOpen(5, 8));
        final RedTemplateProposal proposal = new RedTemplateProposal(template, mock(TemplateContext.class),
                mock(IRegion.class), image, match);

        assertThat(proposal.getDisplayString()).isEqualTo("Some Name - Proposal description text");

        final TextStyle matchStyle = new TextStyle();
        Stylers.Common.MATCH_DECORATION_STYLER.applyStyles(matchStyle);

        final StyleRange[] ranges = proposal.getStyledDisplayString().getStyleRanges();
        assertThat(ranges).hasSize(3);
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(2);
        assertThat(ranges[0].foreground).isEqualTo(matchStyle.foreground);
        assertThat(ranges[0].background).isEqualTo(matchStyle.background);
        assertThat(ranges[0].borderColor).isEqualTo(matchStyle.borderColor);
        assertThat(ranges[1].start).isEqualTo(5);
        assertThat(ranges[1].length).isEqualTo(3);
        assertThat(ranges[1].foreground).isEqualTo(matchStyle.foreground);
        assertThat(ranges[1].background).isEqualTo(matchStyle.background);
        assertThat(ranges[1].borderColor).isEqualTo(matchStyle.borderColor);
        assertThat(ranges[2].start).isEqualTo(10);
        assertThat(ranges[2].length).isEqualTo(27);
        assertThat(ranges[2].foreground).isEqualTo(RedTheme.Colors.getEclipseDecorationColor());
        assertThat(ranges[2].background).isNull();
        assertThat(ranges[2].borderColor).isNull();
    }
}
