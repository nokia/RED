/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Point;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.InformationControlSupport;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentModification;
import org.robotframework.red.graphics.ImagesManager;

public class RedCompletionProposalAdapterTest {

    @Test
    public void contentIsTakenFromAdaptedProposalWithSuffixFromModificationOperation() {
        final AssistProposal proposal = mock(AssistProposal.class);
        when(proposal.getContent()).thenReturn("content");

        final DocumentModification modification1 = new DocumentModification("", new Position(0));
        final DocumentModification modification2 = new DocumentModification(" enhanced", new Position(0));

        final RedCompletionProposalAdapter adapter1 = new RedCompletionProposalAdapter(null, proposal, modification1);
        assertThat(adapter1.getPrefixCompletionText(mock(IDocument.class), 0)).isEqualTo("content");
        final RedCompletionProposalAdapter adapter2 = new RedCompletionProposalAdapter(null, proposal, modification2);
        assertThat(adapter2.getPrefixCompletionText(mock(IDocument.class), 0)).isEqualTo("content enhanced");
    }

    @Test
    public void prefixCompletionStartIsTakenFromModificationToReplacePosition() {
        final DocumentModification modification1 = new DocumentModification("", new Position(10));
        final DocumentModification modification2 = new DocumentModification("", new Position(20), true);
        final DocumentModification modification3 = new DocumentModification("", new Position(30),
                () -> new ArrayList<>());
        final DocumentModification modification4 = new DocumentModification("", new Position(40),
                new Position(100, 200), () -> new ArrayList<>());
        final DocumentModification modification5 = new DocumentModification("", new Position(50),
                new Position(100, 200), true, () -> new ArrayList<>());

        final RedCompletionProposalAdapter adapter1 = new RedCompletionProposalAdapter(null, null, modification1);
        assertThat(adapter1.getPrefixCompletionStart(mock(IDocument.class), 0)).isEqualTo(10);

        final RedCompletionProposalAdapter adapter2 = new RedCompletionProposalAdapter(null, null, modification2);
        assertThat(adapter2.getPrefixCompletionStart(mock(IDocument.class), 0)).isEqualTo(20);

        final RedCompletionProposalAdapter adapter3 = new RedCompletionProposalAdapter(null, null, modification3);
        assertThat(adapter3.getPrefixCompletionStart(mock(IDocument.class), 0)).isEqualTo(30);

        final RedCompletionProposalAdapter adapter4 = new RedCompletionProposalAdapter(null, null, modification4);
        assertThat(adapter4.getPrefixCompletionStart(mock(IDocument.class), 0)).isEqualTo(40);

        final RedCompletionProposalAdapter adapter5 = new RedCompletionProposalAdapter(null, null, modification5);
        assertThat(adapter5.getPrefixCompletionStart(mock(IDocument.class), 0)).isEqualTo(50);
    }

    @Test
    public void imageIsTakenFromAdaptedProposal() {
        final ImageDescriptor imageDescriptor = RedImages.getRobotImage();
        final AssistProposal proposal = mock(AssistProposal.class);
        when(proposal.getImage()).thenReturn(imageDescriptor);

        final DocumentModification modification = new DocumentModification("", new Position(0));
        final RedCompletionProposalAdapter adapter = new RedCompletionProposalAdapter(null, proposal, modification);
        assertThat(adapter.getImage()).isSameAs(ImagesManager.getImage(imageDescriptor));
    }

    @Test
    public void styledLabelIsTakenFromAdaptedProposal() {
        final StyledString label = new StyledString("foo");
        final AssistProposal proposal = mock(AssistProposal.class);
        when(proposal.getStyledLabel()).thenReturn(label);

        final DocumentModification modification = new DocumentModification("", new Position(0));
        final RedCompletionProposalAdapter adapter = new RedCompletionProposalAdapter(null, proposal, modification);
        assertThat(adapter.getStyledDisplayString()).isSameAs(label);
    }

    @Test
    public void labelIsTakenFromAdaptedProposal() {
        final StyledString label = new StyledString("label");
        final AssistProposal proposal = mock(AssistProposal.class);
        when(proposal.getStyledLabel()).thenReturn(label);

        final DocumentModification modification = new DocumentModification("", new Position(0));
        final RedCompletionProposalAdapter adapter = new RedCompletionProposalAdapter(null, proposal, modification);
        assertThat(adapter.getDisplayString()).isEqualTo("label");
    }

    @Test
    public void operationsToPerformAfterAcceptAreTakenFromModification() {
        final Runnable r1 = () -> {
            // empty runnable
        };
        final Runnable r2 = () -> {
            // empty runnable
        };
        final DocumentModification modification = new DocumentModification("", new Position(0),
                () -> newArrayList(r1, r2));
        final RedCompletionProposalAdapter adapter = new RedCompletionProposalAdapter(null, null, modification);

        assertThat(adapter.operationsToPerformAfterAccepting()).containsExactly(r1, r2);
    }

    @Test
    public void assistantShouldBeActivateAfterAccept_whenItIsSpecifiedInModification() {
        final DocumentModification modification1 = new DocumentModification("", new Position(0), true);
        final RedCompletionProposalAdapter adapter1 = new RedCompletionProposalAdapter(null, null, modification1);
        assertThat(adapter1.shouldActivateAssistantAfterAccepting()).isTrue();

        final DocumentModification modification2 = new DocumentModification("", new Position(0), false);
        final RedCompletionProposalAdapter adapter2 = new RedCompletionProposalAdapter(null, null, modification2);
        assertThat(adapter2.shouldActivateAssistantAfterAccepting()).isFalse();
    }

    @Test
    public void contextInfoIsNullByDefault() {
        final AssistProposal proposal = mock(AssistProposal.class);

        final DocumentModification modification = new DocumentModification("", new Position(0));
        final RedCompletionProposalAdapter adapter = new RedCompletionProposalAdapter(null, proposal, modification);
        assertThat(adapter.getContextInformation()).isNull();
    }

    @Test
    public void contextInfoIsProvided_whenPassedToConstructor() {
        final AssistProposal proposal = mock(AssistProposal.class);
        final IContextInformation contextInfo = mock(IContextInformation.class);

        final DocumentModification modification = new DocumentModification("", new Position(0));
        final RedCompletionProposalAdapter adapter = new RedCompletionProposalAdapter(null, proposal, modification,
                contextInfo);
        assertThat(adapter.getContextInformation()).isSameAs(contextInfo);
    }

    @Test
    public void completionProposalsAreComparedUsingLabel() {
        final StyledString label1 = new StyledString("abc");
        final AssistProposal proposal1 = mock(AssistProposal.class);
        when(proposal1.getStyledLabel()).thenReturn(label1);

        final StyledString label2 = new StyledString("xyz");
        final AssistProposal proposal2 = mock(AssistProposal.class);
        when(proposal2.getStyledLabel()).thenReturn(label2);

        final StyledString label3 = new StyledString("def");
        final AssistProposal proposal3 = mock(AssistProposal.class);
        when(proposal3.getStyledLabel()).thenReturn(label3);

        final RedCompletionProposalAdapter adapter1 = new RedCompletionProposalAdapter(null, proposal1, null);
        final RedCompletionProposalAdapter adapter2 = new RedCompletionProposalAdapter(null, proposal2, null);
        final RedCompletionProposalAdapter adapter3 = new RedCompletionProposalAdapter(null, proposal3, null);

        assertThat(adapter1.compareTo(adapter2)).isNegative();
        assertThat(adapter1.compareTo(adapter3)).isNegative();

        assertThat(adapter2.compareTo(adapter3)).isPositive();
        assertThat(adapter2.compareTo(adapter1)).isPositive();

        assertThat(adapter3.compareTo(adapter1)).isPositive();
        assertThat(adapter3.compareTo(adapter2)).isNegative();
    }

    @Test
    public void descriptionIsTakenFromAdaptedProposal_ifThereIsADescription() {
        final InformationControlSupport infoControlSupport = mock(InformationControlSupport.class);
        when(infoControlSupport.isBrowserBased()).thenReturn(false);

        final SuiteSourceAssistantContext context = mock(SuiteSourceAssistantContext.class);
        when(context.getInfoControlSupport()).thenReturn(infoControlSupport);
        
        final AssistProposal proposal = mock(AssistProposal.class);
        when(proposal.isDocumented()).thenReturn(true);
        when(proposal.getDescription()).thenReturn("desc");

        final DocumentModification modification = new DocumentModification("", new Position(0));
        assertThat(new RedCompletionProposalAdapter(context, proposal, modification).getAdditionalProposalInfo())
                .isEqualTo("desc");
    }

    @Test
    public void descriptionIsNull_IfThereIsNoDescription() {
        final AssistProposal proposal = mock(AssistProposal.class);
        when(proposal.isDocumented()).thenReturn(false);
        when(proposal.getDescription()).thenReturn("desc");

        final DocumentModification modification = new DocumentModification("", new Position(0));
        assertThat(new RedCompletionProposalAdapter(null, proposal, modification).getAdditionalProposalInfo()).isNull();
    }

    @Test
    public void contentInDocumentIsProperlyReplacedByProposal() {
        final IDocument document = new Document("document content is here");

        final AssistProposal proposal = mock(AssistProposal.class);
        when(proposal.getContent()).thenReturn("stuff");

        final DocumentModification modification = new DocumentModification(" with suffix",
                new Position(9, 7));

        final RedCompletionProposalAdapter adapter = new RedCompletionProposalAdapter(null, proposal, modification);
        adapter.apply(document);
        assertThat(document).isEqualTo(new Document("document stuff with suffix is here"));
    }

    @Test
    public void selectionIsTakenFromModification_whenProvided() {
        final DocumentModification modification = new DocumentModification("", new Position(0),
                new Position(42, 84), () -> new ArrayList<>());

        final RedCompletionProposalAdapter adapter = new RedCompletionProposalAdapter(null, null, modification);
        assertThat(adapter.getSelection(mock(IDocument.class))).isEqualTo(new Point(42, 84));
    }

    @Test
    public void selectionIsCalculatedFromContentWhichWillBeInserted_whenModificationDoesNotProvideSelection() {
        final AssistProposal proposal = mock(AssistProposal.class);
        when(proposal.getContent()).thenReturn("stuff");

        final DocumentModification modification = new DocumentModification("suffix", new Position(15));

        final RedCompletionProposalAdapter adapter = new RedCompletionProposalAdapter(null, proposal, modification);
        assertThat(adapter.getSelection(mock(IDocument.class))).isEqualTo(new Point(26, 0));
    }
}
