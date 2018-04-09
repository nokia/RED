/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.Range;

public class RedFileLocationProposalTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RedFileLocationProposalTest.class);

    @BeforeClass
    public static void beforeSuite() throws IOException, CoreException {
        projectProvider.createFile("file.txt");
    }

    @Test
    public void testProposalWithEmptyContentAndEmptyMatch() {
        final IFile file = projectProvider.getFile("file.txt");
        final RedFileLocationProposal proposal = new RedFileLocationProposal("", "label", file, ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEmpty();
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isNotNull();
        assertThat(proposal.getLabel()).isEqualTo("label");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("label");
        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void testProposalWithNonEmptyContentAndEmptyMatch() {
        final IFile file = projectProvider.getFile("file.txt");
        final RedFileLocationProposal proposal = new RedFileLocationProposal("content", "label", file,
                ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("content");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isNotNull();
        assertThat(proposal.getLabel()).isEqualTo("label");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("label");
        assertThat(proposal.getStyledLabel().getStyleRanges()).isEmpty();
        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void testProposalWithNonEmptyContentAndNonEmptyMatch() {
        final IFile file = projectProvider.getFile("file.txt");
        final RedFileLocationProposal proposal = new RedFileLocationProposal("content", "label", file,
                new ProposalMatch(Range.closedOpen(1, 3), Range.closedOpen(4, 6)));

        assertThat(proposal.getContent()).isEqualTo("content");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isNotNull();
        assertThat(proposal.getLabel()).isEqualTo("label");

        assertThat(proposal.getStyledLabel().getString()).isEqualTo("label");
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
        assertThat(ranges[1].length).isEqualTo(1);

        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }
}
