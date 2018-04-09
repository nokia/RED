/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal.VariableOrigin;

import com.google.common.collect.Range;

public class RedVariableProposalTest {

    @Test
    public void originTest() {
        final RedVariableProposal proposal1 = new RedVariableProposal("name", "source", "val", "comment",
                VariableOrigin.BUILTIN, ProposalMatch.EMPTY);
        assertThat(proposal1.getOrigin()).isEqualTo(VariableOrigin.BUILTIN);

        final RedVariableProposal proposal2 = new RedVariableProposal("name", "source", "val", "comment",
                VariableOrigin.IMPORTED, ProposalMatch.EMPTY);
        assertThat(proposal2.getOrigin()).isEqualTo(VariableOrigin.IMPORTED);

        final RedVariableProposal proposal3 = new RedVariableProposal("name", "source", "val", "comment",
                VariableOrigin.LOCAL, ProposalMatch.EMPTY);
        assertThat(proposal3.getOrigin()).isEqualTo(VariableOrigin.LOCAL);
    }

    @Test
    public void imagesTest() {
        final RedVariableProposal proposal1 = new RedVariableProposal("${var}", "source", "val", "comment",
                VariableOrigin.BUILTIN, ProposalMatch.EMPTY);
        assertThat(proposal1.getImage()).isEqualTo(RedImages.getRobotScalarVariableImage());

        final RedVariableProposal proposal2 = new RedVariableProposal("@{var}", "source", "val", "comment",
                VariableOrigin.BUILTIN, ProposalMatch.EMPTY);
        assertThat(proposal2.getImage()).isEqualTo(RedImages.getRobotListVariableImage());

        final RedVariableProposal proposal3 = new RedVariableProposal("&{var}", "source", "val", "comment",
                VariableOrigin.BUILTIN, ProposalMatch.EMPTY);
        assertThat(proposal3.getImage()).isEqualTo(RedImages.getRobotDictionaryVariableImage());
    }

    @Test
    public void descriptionsTest() {
        final RedVariableProposal proposal0 = new RedVariableProposal("${var}", "", "", "", VariableOrigin.BUILTIN,
                ProposalMatch.EMPTY);
        assertThat(proposal0.isDocumented()).isTrue();
        assertThat(proposal0.getDescription()).isEqualTo("Source: ");

        final RedVariableProposal proposal1 = new RedVariableProposal("${var}", "source", "", "",
                VariableOrigin.BUILTIN, ProposalMatch.EMPTY);
        assertThat(proposal1.isDocumented()).isTrue();
        assertThat(proposal1.getDescription()).isEqualTo("Source: source");

        final RedVariableProposal proposal2 = new RedVariableProposal("@{var}", "source", "val", "",
                VariableOrigin.BUILTIN, ProposalMatch.EMPTY);
        assertThat(proposal2.isDocumented()).isTrue();
        assertThat(proposal2.getDescription()).isEqualTo("Source: source\nValue: val");

        final RedVariableProposal proposal3 = new RedVariableProposal("&{var}", "source", "", "comment",
                VariableOrigin.BUILTIN, ProposalMatch.EMPTY);
        assertThat(proposal3.isDocumented()).isTrue();
        assertThat(proposal3.getDescription()).isEqualTo("Source: source\nComment: comment");

        final RedVariableProposal proposal4 = new RedVariableProposal("&{var}", "source", "val", "comment",
                VariableOrigin.BUILTIN, ProposalMatch.EMPTY);
        assertThat(proposal4.isDocumented()).isTrue();
        assertThat(proposal4.getDescription()).isEqualTo("Source: source\nValue: val\nComment: comment");
    }

    @Test
    public void styledLabelIsMadeJustFromVariableName() {
        final RedVariableProposal proposal = new RedVariableProposal("${var}", "source", "val", "comment",
                VariableOrigin.BUILTIN, ProposalMatch.EMPTY);

        final StyledString label = proposal.getStyledLabel();

        assertThat(label.getString()).isEqualTo("${var}");
        assertThat(label.getStyleRanges()).hasSize(0);
    }

    @Test
    public void styledLabelIsMadeJustFromVariableName_andMatchIsHighlighted() {
        final RedVariableProposal proposal = new RedVariableProposal("${var}", "source", "val", "comment",
                VariableOrigin.BUILTIN, new ProposalMatch(Range.closedOpen(0, 2)));

        final StyledString label = proposal.getStyledLabel();

        assertThat(label.getString()).isEqualTo("${var}");
        assertThat(label.getStyleRanges()).hasSize(1);

        final TextStyle matchStyle = new TextStyle();
        Stylers.Common.MARKED_PREFIX_STYLER.applyStyles(matchStyle);

        assertThat(label.getStyleRanges()[0].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(label.getStyleRanges()[0].foreground.getRGB()).isEqualTo(matchStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[0].borderColor.getRGB()).isEqualTo(matchStyle.borderColor.getRGB());
        assertThat(label.getStyleRanges()[0].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(label.getStyleRanges()[0].strikeout).isFalse();
        assertThat(label.getStyleRanges()[0].start).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(2);
    }

    @Test
    public void hashCodeIsMadeFromContentSourceAndOrigin() {
        final String name = "name";
        final String source = "source";
        final String value = "value";
        final String comment = "comment";
        final VariableOrigin origin = VariableOrigin.BUILTIN;
        final RedVariableProposal proposal = new RedVariableProposal(name, source, value, comment, origin,
                ProposalMatch.EMPTY);

        assertThat(proposal.hashCode()).isEqualTo(Objects.hash(name, source, origin));
    }

    @Test
    public void equalityTests() {
        final RedVariableProposal proposal = new RedVariableProposal("name", "source", "value", "comment",
                VariableOrigin.BUILTIN, ProposalMatch.EMPTY);

        assertThat(proposal.equals(null)).isFalse();
        assertThat(proposal.equals(new Object())).isFalse();
        assertThat(proposal.equals(new RedVariableProposal("other_name", "source", "value", "comment",
                VariableOrigin.BUILTIN, ProposalMatch.EMPTY))).isFalse();
        assertThat(proposal.equals(new RedVariableProposal("name", "other_source", "value", "comment",
                VariableOrigin.BUILTIN, ProposalMatch.EMPTY))).isFalse();
        assertThat(proposal.equals(new RedVariableProposal("name", "source", "value", "comment",
                VariableOrigin.IMPORTED, ProposalMatch.EMPTY))).isFalse();

        assertThat(proposal.equals(new RedVariableProposal("name", "source", "value", "comment", VariableOrigin.BUILTIN,
                ProposalMatch.EMPTY))).isTrue();
        assertThat(proposal.equals(new RedVariableProposal("name", "source", "other_value", "comment",
                VariableOrigin.BUILTIN, ProposalMatch.EMPTY))).isTrue();
        assertThat(proposal.equals(new RedVariableProposal("name", "source", "value", "other_comment",
                VariableOrigin.BUILTIN, ProposalMatch.EMPTY))).isTrue();
    }
}
