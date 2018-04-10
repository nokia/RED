/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.red.jface.assist.RedContentProposal.ModificationStrategy;
import org.robotframework.red.jface.assist.RedTextContentAdapter.SubstituteTextModificationStrategy;

public class AssistProposalAdapterTest {

    @Test
    public void contentIsTakenFromAdaptedProposalWithAddition() {
        final AssistProposal proposal = mock(AssistProposal.class);
        when(proposal.getContent()).thenReturn("content");

        assertThat(new AssistProposalAdapter(null, proposal).getContent()).isEqualTo("content");
        assertThat(new AssistProposalAdapter(null, proposal, " enhanced").getContent()).isEqualTo("content enhanced");
    }

    @Test
    public void cursorPositionIsAlwaysAtTheEndOfContent() {
        final AssistProposal proposal = mock(AssistProposal.class);
        when(proposal.getContent()).thenReturn("content");

        assertThat(new AssistProposalAdapter(null, proposal).getCursorPosition()).isEqualTo(7);
        assertThat(new AssistProposalAdapter(null, proposal, " enhanced").getCursorPosition()).isEqualTo(16);
    }

    @Test
    public void imageIsTakenFromAdaptedProposal() {
        final ImageDescriptor imageDescriptor = RedImages.getRobotImage();
        final AssistProposal proposal = mock(AssistProposal.class);
        when(proposal.getImage()).thenReturn(imageDescriptor);

        assertThat(new AssistProposalAdapter(null, proposal).getImage()).isSameAs(imageDescriptor);
    }

    @Test
    public void styledLabelIsTakenFromAdaptedProposal() {
        final StyledString label = new StyledString("foo");
        final AssistProposal proposal = mock(AssistProposal.class);
        when(proposal.getStyledLabel()).thenReturn(label);

        assertThat(new AssistProposalAdapter(null, proposal).getStyledLabel()).isSameAs(label);
    }

    @Test
    public void labelIsTakenFromAdaptedProposal() {
        final AssistProposal proposal = mock(AssistProposal.class);
        when(proposal.getLabel()).thenReturn("label");

        assertThat(new AssistProposalAdapter(null, proposal).getLabel()).isSameAs("label");
    }

    @Test
    public void hasDescriptionIsTakenFromAdaptedProposal() {
        final AssistProposal proposal1 = mock(AssistProposal.class);
        when(proposal1.isDocumented()).thenReturn(true);

        final AssistProposal proposal2 = mock(AssistProposal.class);
        when(proposal2.isDocumented()).thenReturn(false);

        assertThat(new AssistProposalAdapter(null, proposal1).hasDescription()).isTrue();
        assertThat(new AssistProposalAdapter(null, proposal2).hasDescription()).isFalse();
    }

    @Test
    public void descriptionIsTakenFromAdaptedProposal() {
        final AssistProposal proposal1 = mock(AssistProposal.class);
        when(proposal1.getDescription()).thenReturn("desc");

        assertThat(new AssistProposalAdapter(null, proposal1).getDescription()).isEqualTo("desc");
    }

    @Test
    public void substituteStrategyIsAlwaysProvided_whenNoOtherStrategyWasPassedToConstructor() {
        final AssistProposal proposal = mock(AssistProposal.class);

        assertThat(new AssistProposalAdapter(null, proposal).getModificationStrategy())
                .isInstanceOf(SubstituteTextModificationStrategy.class);
        assertThat(new AssistProposalAdapter(null, proposal, "").getModificationStrategy())
                .isInstanceOf(SubstituteTextModificationStrategy.class);
        assertThat(new AssistProposalAdapter(null, proposal, (ModificationStrategy) null).getModificationStrategy())
                .isInstanceOf(SubstituteTextModificationStrategy.class);
    }

    @Test
    public void modificationStrategyIsProvided_whenPassedToConstructor() {
        final AssistProposal proposal = mock(AssistProposal.class);
        final ModificationStrategy strategy = mock(ModificationStrategy.class);

        assertThat(new AssistProposalAdapter(null, proposal, strategy).getModificationStrategy()).isSameAs(strategy);
    }

    @Test
    public void substituteStrategyWithCorrectCommitAfterInsertFlagIsProvided() {
        final AssistProposal proposal = mock(AssistProposal.class);
        when(proposal.getContent()).thenReturn("kw name ${arg}");

        assertThat(
                new AssistProposalAdapter(null, proposal, p -> !p.getContent().contains("$")).getModificationStrategy())
                .isInstanceOfSatisfying(SubstituteTextModificationStrategy.class,
                        strategy -> assertThat(strategy.shouldCommitAfterInsert()).isFalse());
        assertThat(
                new AssistProposalAdapter(null, proposal, p -> p.getContent().contains("$")).getModificationStrategy())
                .isInstanceOfSatisfying(SubstituteTextModificationStrategy.class,
                        strategy -> assertThat(strategy.shouldCommitAfterInsert()).isTrue());
        assertThat(new AssistProposalAdapter(null, proposal, p -> !p.getContent().contains("$"), ArrayList::new)
                .getModificationStrategy()).isInstanceOfSatisfying(SubstituteTextModificationStrategy.class,
                        strategy -> assertThat(strategy.shouldCommitAfterInsert()).isFalse());
        assertThat(new AssistProposalAdapter(null, proposal, p -> p.getContent().contains("$"), ArrayList::new)
                .getModificationStrategy()).isInstanceOfSatisfying(SubstituteTextModificationStrategy.class,
                        strategy -> assertThat(strategy.shouldCommitAfterInsert()).isTrue());
    }

    @Test
    public void emptyOperationsToPerformAfterAcceptingAreAlwaysProvided_whenNoOperationsPassedToConstructor() {
        final AssistProposal proposal = mock(AssistProposal.class);
        final ModificationStrategy strategy = mock(ModificationStrategy.class);

        assertThat(new AssistProposalAdapter(null, proposal).getOperationsToPerformAfterAccepting()).isEmpty();
        assertThat(new AssistProposalAdapter(null, proposal, p -> true).getOperationsToPerformAfterAccepting())
                .isEmpty();
        assertThat(new AssistProposalAdapter(null, proposal, strategy).getOperationsToPerformAfterAccepting())
                .isEmpty();
        assertThat(new AssistProposalAdapter(null, proposal, " ").getOperationsToPerformAfterAccepting()).isEmpty();
    }

    @Test
    public void operationsToPerformAfterAcceptingAreProvided_whenPassedToConstructor() {
        final AssistProposal proposal = mock(AssistProposal.class);
        final Runnable operation1 = () -> {
        };
        final Runnable operation2 = () -> {
        };

        assertThat(new AssistProposalAdapter(null, proposal, p -> true, () -> Arrays.asList(operation1, operation2))
                .getOperationsToPerformAfterAccepting()).containsExactly(operation1, operation2);
    }
}
