/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.EnumSet;

import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.Test;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.collect.Range;

public class RedImportProposalTest {

    @Test
    public void itIsNotPossibleToCreateProposalForModelTypeDifferentThanLibraryOrResourceImport() {
        for (final ModelType type : EnumSet
                .complementOf(EnumSet.of(ModelType.RESOURCE_IMPORT_SETTING, ModelType.LIBRARY_IMPORT_SETTING))) {
            try {
                new RedImportProposal("content", "bdd", type, ProposalMatch.EMPTY);
                fail();
            } catch (final IllegalArgumentException e) {
                // this is expected
            }
        }
    }

    @Test
    public void testProposalWithNonEmptyBddPrefixAndEmptyMatch() {
        final RedImportProposal proposal = new RedImportProposal("res", "given ",
                ModelType.RESOURCE_IMPORT_SETTING, ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("given res.");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotFileImage());
        assertThat(proposal.getLabel()).isEqualTo("res");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("res");
        assertThat(proposal.getStyledLabel().getStyleRanges()).isEmpty();
        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void testProposalWithEmptyBddPrefixAndEmptyMatch() {
        final RedImportProposal proposal = new RedImportProposal("lib", "", ModelType.LIBRARY_IMPORT_SETTING,
                ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("lib.");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getLibraryImage());
        assertThat(proposal.getLabel()).isEqualTo("lib");
        assertThat(proposal.getStyledLabel().getString()).isEqualTo("lib");
        assertThat(proposal.getStyledLabel().getStyleRanges()).isEmpty();
        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }

    @Test
    public void testProposalWithNonEmptyBddPrefixAndNonEmptyMatch() {
        final RedImportProposal proposal = new RedImportProposal("lib", "given ",
                ModelType.LIBRARY_IMPORT_SETTING, new ProposalMatch(Range.closedOpen(0, 3)));

        assertThat(proposal.getContent()).isEqualTo("given lib.");
        assertThat(proposal.getArguments()).isEmpty();
        assertThat(proposal.getImage()).isNotNull();
        assertThat(proposal.getLabel()).isEqualTo("lib");

        assertThat(proposal.getStyledLabel().getString()).isEqualTo("lib");
        final TextStyle matchStyle = new TextStyle();
        Stylers.Common.MARKED_PREFIX_STYLER.applyStyles(matchStyle);

        final StyleRange[] ranges = proposal.getStyledLabel().getStyleRanges();
        assertThat(ranges).hasSize(1);

        assertThat(ranges[0].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(matchStyle.foreground.getRGB());
        assertThat(ranges[0].borderColor.getRGB()).isEqualTo(matchStyle.borderColor.getRGB());
        assertThat(ranges[0].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(ranges[0].strikeout).isFalse();
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(3);

        assertThat(proposal.isDocumented()).isFalse();
        assertThat(proposal.getDescription()).isEmpty();
    }
}
