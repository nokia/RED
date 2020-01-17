/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.red.jface.viewers.Stylers;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

import com.google.common.collect.Range;

@ExtendWith(ProjectExtension.class)
public class RedFileLocationProposalTest {

    @Project(files = { "file.txt" })
    static IProject project;

    @Test
    public void testProposalWithEmptyContentAndEmptyMatch() {
        final IFile file = getFile(project, "file.txt");
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
        final IFile file = getFile(project, "file.txt");
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
        final IFile file = getFile(project, "file.txt");
        final RedFileLocationProposal proposal = new RedFileLocationProposal("content", "label", file,
                new ProposalMatch(Range.closedOpen(1, 3), Range.closedOpen(4, 6)));

        assertThat(proposal.getContent()).isEqualTo("content");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isNotNull();
        assertThat(proposal.getLabel()).isEqualTo("label");

        assertThat(proposal.getStyledLabel().getString()).isEqualTo("label");
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
        assertThat(ranges[1].length).isEqualTo(1);

        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }
}
