/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.red.graphics.ImagesManager;

public class RedCompletionBuilderTest {

    @Test
    public void selectionIsProperlySet_1() {
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut("def")
                .byInsertingAt(0)
                .thenCursorWillStopAt(42)
                .create();

        final IDocument document = mock(IDocument.class);
        assertThat(proposal.getSelection(document)).isEqualTo(new Point(42, 0));
    }

    @Test
    public void selectionIsProperlySet_2() {
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut("def")
                .byInsertingAt(5)
                .thenCursorWillStopAtTheEndOfInsertion()
                .create();

        final IDocument document = mock(IDocument.class);
        assertThat(proposal.getSelection(document)).isEqualTo(new Point(8, 0));
    }

    @Test
    public void selectionIsProperlySet_3() {
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut("def")
                .byInsertingAt(5)
                .thenCursorWillStopBeforeEnd(2)
                .create();

        final IDocument document = mock(IDocument.class);
        assertThat(proposal.getSelection(document)).isEqualTo(new Point(6, 0));
    }

    @Test
    public void assistantActivationIsProperlySet() {
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut("def")
                .byInsertingAt(5)
                .activateAssistantAfterAccepting(true)
                .thenCursorWillStopAt(10)
                .create();
        assertThat(proposal.shouldActivateAssitantAfterAccepting()).isTrue();
    }

    @Test
    public void htmlAdditionalInfoIsProperlySet() {
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut("def")
                .byInsertingAt(5)
                .secondaryPopupShouldBeDisplayedUsingHtml("info")
                .thenCursorWillStopAt(10)
                .create();
        assertThat(proposal.getAdditionalProposalInfo()).isEqualTo("info");
    }

    @Test
    public void operationsToPerformAreProperlySet() {
        final Runnable op1 = new Runnable() {
            @Override
            public void run() { }
        };
        final Runnable op2 = new Runnable() {
            @Override
            public void run() { }
        };
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut("def")
                .byInsertingAt(5)
                .performAfterAccepting(newArrayList(op1, op2))
                .thenCursorWillStopAt(10)
                .create();
        assertThat(proposal.operationsToPerformAfterAccepting()).containsExactly(op1, op2);
    }

    @Test
    public void imageForProposalIsProperlySet() {
        final Image someImage = ImagesManager.getImage(RedImages.getErrorImage());
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut("def")
                .byInsertingAt(3)
                .thenCursorWillStopAt(0)
                .proposalsShouldHaveIcon(someImage)
                .create();
        assertThat(proposal.getImage()).isSameAs(someImage);
    }

    @Test
    public void labelToDisplayIsProperlySet() {
        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut("def")
                .byInsertingAt(3)
                .thenCursorWillStopAt(0)
                .displayedLabelShouldBe("label")
                .create();

        assertThat(proposal.getDisplayString()).isEqualTo("label");
    }

    @Test
    public void simpleInsertingProposalTest() {
        final IDocument document = new Document("abcghij");

        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut("def")
                .byInsertingAt(3)
                .thenCursorWillStopAt(0)
                .create();

        proposal.apply(document);
        assertThat(document).isEqualTo(new Document("abcdefghij"));
    }

    @Test
    public void simpleReplacingProposalTest_1() {
        final IDocument document = new Document("abcghij");

        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut("def")
                .byReplacingRegion(3, 4)
                .thenCursorWillStopAt(0)
                .create();

        proposal.apply(document);
        assertThat(document).isEqualTo(new Document("abcdef"));
    }

    @Test
    public void simpleReplacingProposalTest_2() {
        final IDocument document = new Document("abcghij");

        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                .willPut("def")
                .byReplacingRegion(new Region(3, 4))
                .thenCursorWillStopAt(0)
                .create();

        proposal.apply(document);
        assertThat(document).isEqualTo(new Document("abcdef"));
    }

}
