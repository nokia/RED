/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.Test;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.collect.Range;

public class RedLibraryProposalTest {

    @Test
    public void testProposalWithEmptyContentAndEmptyMatch() {
        final LibrarySpecification spec = LibrarySpecification.create("");
        spec.setDescriptor(new LibraryDescriptor("", LibraryType.PYTHON, "path", new ArrayList<>()));
        spec.setDocumentation("desc");
        
        final RedLibraryProposal proposal = new RedLibraryProposal(null, spec, false, ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEmpty();
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.isImported()).isFalse();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getLibraryImage());
        assertThat(proposal.getLabel()).isEmpty();
        assertThat(proposal.getStyledLabel().length()).isEqualTo(0);
        assertThat(proposal.isDocumented()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("desc");
    }

    @Test
    public void testProposalWithNonEmptyContentEmptyArgumentsAndEmptyMatch() {
        final LibrarySpecification spec = LibrarySpecification.create("content");
        spec.setDescriptor(new LibraryDescriptor("content", LibraryType.PYTHON, "path", new ArrayList<>()));
        spec.setDocumentation("desc");

        final RedLibraryProposal proposal = new RedLibraryProposal(null, spec, false, ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("content");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.isImported()).isFalse();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getLibraryImage());
        assertThat(proposal.getLabel()).isEqualTo("content");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("content");
        assertThat(proposal.getStyledLabel().getStyleRanges()).isEmpty();
        assertThat(proposal.isDocumented()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("desc");
    }

    @Test
    public void testProposalWithNonEmptyContentNonEmptyArgumentsAndEmptyMatch() {
        final LibrarySpecification spec = LibrarySpecification.create("content");
        spec.setDescriptor(new LibraryDescriptor("content", LibraryType.PYTHON, "path", newArrayList("arg1", "arg2")));
        spec.setDocumentation("desc");

        final RedLibraryProposal proposal = new RedLibraryProposal(null, spec, false, ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("content");
        assertThat(proposal.getArguments()).containsExactly("arg1", "arg2");
        assertThat(proposal.isImported()).isFalse();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getLibraryImage());
        assertThat(proposal.getLabel()).isEqualTo("content arg1 arg2");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("content arg1 arg2");
        assertThat(proposal.getStyledLabel().getStyleRanges()).isEmpty();
        assertThat(proposal.isDocumented()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("desc");
    }

    @Test
    public void testAlreadyImportedProposalWithNonEmptyContentNonEmptyArgumentsAndEmptyMatch() {
        final LibrarySpecification spec = LibrarySpecification.create("content");
        spec.setDescriptor(new LibraryDescriptor("content", LibraryType.PYTHON, "path", newArrayList("arg1", "arg2")));
        spec.setDocumentation("desc");

        final RedLibraryProposal proposal = new RedLibraryProposal(null, spec, true, ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("content");
        assertThat(proposal.getArguments()).containsExactly("arg1", "arg2");
        assertThat(proposal.isImported()).isTrue();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getLibraryImage());
        assertThat(proposal.getLabel()).isEqualTo("content arg1 arg2");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("content arg1 arg2 (already imported)");

        final TextStyle decorationStyle = new TextStyle();
        Stylers.Common.ECLIPSE_DECORATION_STYLER.applyStyles(decorationStyle);

        final StyleRange[] ranges = proposal.getStyledLabel().getStyleRanges();
        assertThat(ranges).hasSize(1);
        assertThat(ranges[0].background).isNull();
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(decorationStyle.foreground.getRGB());
        assertThat(ranges[0].borderColor).isNull();
        assertThat(ranges[0].borderStyle).isEqualTo(decorationStyle.borderStyle);
        assertThat(ranges[0].strikeout).isFalse();
        assertThat(ranges[0].start).isEqualTo(17);
        assertThat(ranges[0].length).isEqualTo(19);

        assertThat(proposal.isDocumented()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("desc");
    }

    @Test
    public void testAlreadyImportedProposalWithNonEmptyContentNonEmptyArgumentsAndNonEmptyMatch() {
        final LibrarySpecification spec = LibrarySpecification.create("content");
        spec.setDescriptor(new LibraryDescriptor("content", LibraryType.PYTHON, "path", newArrayList("arg1", "arg2")));
        spec.setDocumentation("desc");

        final RedLibraryProposal proposal = new RedLibraryProposal(null, spec, true,
                new ProposalMatch(Range.closedOpen(1, 5)));

        assertThat(proposal.getContent()).isEqualTo("content");
        assertThat(proposal.getArguments()).containsExactly("arg1", "arg2");
        assertThat(proposal.isImported()).isTrue();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getLibraryImage());
        assertThat(proposal.getLabel()).isEqualTo("content arg1 arg2");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("content arg1 arg2 (already imported)");

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
        assertThat(ranges[1].start).isEqualTo(17);
        assertThat(ranges[1].length).isEqualTo(19);

        assertThat(proposal.isDocumented()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("desc");
    }
}
