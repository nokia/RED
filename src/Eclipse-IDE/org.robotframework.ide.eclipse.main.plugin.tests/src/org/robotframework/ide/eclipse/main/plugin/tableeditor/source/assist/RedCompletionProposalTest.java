/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.red.graphics.ImagesManager;

public class RedCompletionProposalTest {

    @Test(expected = NullPointerException.class)
    public void nullCannotBePassedAsReplacementString() {
        new RedCompletionProposal(null, 0, 0, 0, 0, 0, null, false, null, true, new ArrayList<Runnable>(), null, false);
    }

    @Test(expected = IllegalStateException.class)
    public void negativeNumberCannotBePassedAsReplacementOffset() {
        new RedCompletionProposal("", -10, 0, 0, 0, 0, null, false, null, true, new ArrayList<Runnable>(), null, false);
    }

    @Test(expected = IllegalStateException.class)
    public void negativeNumberCannotBePassedAsReplacementLength() {
        new RedCompletionProposal("", 0, -10, 0, 0, 0, null, false, null, true, new ArrayList<Runnable>(), null, false);
    }

    @Test(expected = IllegalStateException.class)
    public void negativeNumberCannotBePassedAsCursorPosition() {
        new RedCompletionProposal("", 0, 0, 0, -50, 0, null, false, null, true, new ArrayList<Runnable>(), null, false);
    }

    @Test
    public void shouldActivateProposalsAfterAccepting_isProperlyReturned() {
        final RedCompletionProposal proposal1 = new RedCompletionProposal("", 0, 0, 0, 0, 0, null, false, null, true,
                new ArrayList<Runnable>(), null, false);

        final RedCompletionProposal proposal2 = new RedCompletionProposal("", 0, 0, 0, 0, 0, null, false, null, false,
                new ArrayList<Runnable>(), null, false);

        assertThat(proposal1.shouldActivateAssitantAfterAccepting()).isTrue();
        assertThat(proposal2.shouldActivateAssitantAfterAccepting()).isFalse();
    }

    @Test
    public void operationsToPerformAreProperlyReturned() {
        final Runnable op1 = new Runnable() {
            @Override
            public void run() { }
        };
        final Runnable op2 = new Runnable() {
            @Override
            public void run() { }
        };
        final RedCompletionProposal proposal1 = new RedCompletionProposal("", 0, 0, 0, 0, 0, null, false, null, false,
                new ArrayList<Runnable>(), null, false);
        final RedCompletionProposal proposal2 = new RedCompletionProposal("", 0, 0, 0, 0, 0, null, false, null, false,
                newArrayList(op1), null, false);
        final RedCompletionProposal proposal3 = new RedCompletionProposal("", 0, 0, 0, 0, 0, null, false, null, false,
                newArrayList(op1, op2), null, false);

        assertThat(proposal1.operationsToPerformAfterAccepting()).isEmpty();
        assertThat(proposal2.operationsToPerformAfterAccepting()).containsExactly(op1);
        assertThat(proposal3.operationsToPerformAfterAccepting()).containsExactly(op1, op2);
    }

    @Test
    public void prefixCompletionTextIsProperlyReturned() {
        final RedCompletionProposal proposal = new RedCompletionProposal("replacement", 0, 0, 0, 0, 0, null, false,
                null, false, new ArrayList<Runnable>(), null, false);

        final IDocument document = mock(IDocument.class);
        assertThat(proposal.getPrefixCompletionText(document, 0)).isEqualTo("replacement");

        verifyZeroInteractions(document);
    }

    @Test
    public void prefixCompletionOffsetIsProperlyReturned() {
        final RedCompletionProposal proposal = new RedCompletionProposal("", 42, 0, 0, 0, 0, null, false, null, false,
                new ArrayList<Runnable>(), null, false);

        final IDocument document = mock(IDocument.class);
        assertThat(proposal.getPrefixCompletionStart(document, 0)).isEqualTo(42);

        verifyZeroInteractions(document);
    }

    @Test
    public void additionalProposalInfoIsProperlyReturned() {
        final RedCompletionProposal proposal = new RedCompletionProposal("", 0, 0, 0, 0, 0, null, false, null, false,
                new ArrayList<Runnable>(), "additional", false);

        assertThat(proposal.getAdditionalProposalInfo()).isEqualTo("additional");
    }

    @Test
    public void displayStringIsReturned_whenLabelIsProvided() {
        final RedCompletionProposal proposal = new RedCompletionProposal("", 0, 0, 0, 0, 0, null, false,
                "label", false, new ArrayList<Runnable>(), null, false);

        assertThat(proposal.getDisplayString()).isEqualTo("label");
    }

    @Test
    public void replacementStringIsUsedAsDisplayString_whenLabelIsNull() {
        final RedCompletionProposal proposal = new RedCompletionProposal("replacement", 0, 0, 0, 0, 0, null, false,
                null, false, new ArrayList<Runnable>(), null, false);

        assertThat(proposal.getDisplayString()).isEqualTo("replacement");
    }

    @Test
    public void styledLabelIsConstructedWithDisplayStringOnly_whenMatchingPrefixHasZeroLengthButPrefixShouldNotBeDecorated() {
        final RedCompletionProposal proposal = new RedCompletionProposal("", 0, 0, 0, 0, 0, null, false, "label", false,
                new ArrayList<Runnable>(), null, false);

        final StyledString label = proposal.getStyledDisplayString();

        assertThat(label.getString()).isEqualTo("label");
        assertThat(label.getStyleRanges()).isEmpty();
    }

    @Test
    public void styledLabelIsConstructedWithDisplayStringOnly_whenMatchingPrefixHasPositiveLengthButPrefixShouldNotBeDecorated() {
        final RedCompletionProposal proposal = new RedCompletionProposal("", 0, 0, 3, 0, 0, null, false, "label", false,
                new ArrayList<Runnable>(), null, false);

        final StyledString label = proposal.getStyledDisplayString();

        assertThat(label.getString()).isEqualTo("label");
        assertThat(label.getStyleRanges()).isEmpty();
    }

    @Test
    public void styledLabelHavePrefixHihglighted_whenMatchingPrefixHasPositiveLengthAndItShouldBeDecorated() {
        final RedCompletionProposal proposal = new RedCompletionProposal("", 0, 0, 3, 0, 0, null, true, "label", false,
                new ArrayList<Runnable>(), null, false);

        final StyledString label = proposal.getStyledDisplayString();

        assertThat(label.getString()).isEqualTo("label");

        final TextStyle matchStyle = new TextStyle();
        Stylers.Common.MARKED_PREFIX_STYLER.applyStyles(matchStyle);

        final StyleRange[] ranges = label.getStyleRanges();
        assertThat(ranges).hasSize(1);

        assertThat(ranges[0].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(matchStyle.foreground.getRGB());
        assertThat(ranges[0].borderColor.getRGB()).isEqualTo(matchStyle.borderColor.getRGB());
        assertThat(ranges[0].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(ranges[0].strikeout).isFalse();
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(3);
    }

    @Test
    public void imageIsProperlyReturned() {
        final Image someImage = ImagesManager.getImage(RedImages.getSourceImage());
        final RedCompletionProposal proposal = new RedCompletionProposal("", 0, 0, 0, 0, 0, someImage, false, null,
                false, new ArrayList<Runnable>(), null, false);

        assertThat(proposal.getImage()).isSameAs(someImage);
    }

    @Test
    public void selectionIsCalculatedFromCursorPositionAndReplacementOffset() {
        final RedCompletionProposal proposal = new RedCompletionProposal("replacement", 34, 0, 0, 56, 20, null, false,
                null, false, new ArrayList<Runnable>(), null, false);

        assertThat(proposal.getSelection(mock(IDocument.class))).isEqualTo(new Point(90, 20));
    }

    @Test
    public void contextInformationIsNull() {
        final RedCompletionProposal proposal = new RedCompletionProposal("replacement", 34, 0, 0, 56, 20, null, false,
                null, false, new ArrayList<Runnable>(), null, false);

        assertThat(proposal.getContextInformation()).isNull();
    }

    @Test
    public void completionProposalsAreComparedUsingLabel() {
        final RedCompletionProposal proposal1 = new RedCompletionProposal("", 34, 0, 0, 56, 20, null, false,
                "abc", false, new ArrayList<Runnable>(), null, false);
        
        final RedCompletionProposal proposal2 = new RedCompletionProposal("", 34, 0, 0, 56, 20, null, false, "xyz",
                false, new ArrayList<Runnable>(), null, false);

        final RedCompletionProposal proposal3 = new RedCompletionProposal("", 34, 0, 0, 56, 20, null, false, "def",
                false, new ArrayList<Runnable>(), null, false);

        assertThat(proposal1.compareTo(proposal2)).isNegative();
        assertThat(proposal1.compareTo(proposal3)).isNegative();

        assertThat(proposal2.compareTo(proposal3)).isPositive();
        assertThat(proposal2.compareTo(proposal1)).isPositive();

        assertThat(proposal3.compareTo(proposal1)).isPositive();
        assertThat(proposal3.compareTo(proposal2)).isNegative();
    }

    @Test
    public void informationContentCreatorIsAlwaysNew() {
        final RedCompletionProposal proposal = new RedCompletionProposal("", 34, 0, 0, 56, 20, null, false,
                null, false, new ArrayList<Runnable>(), null, false);

        final IInformationControlCreator creator1 = proposal.getInformationControlCreator();
        final IInformationControlCreator creator2 = proposal.getInformationControlCreator();

        assertThat(creator1).isNotSameAs(creator2);
    }

    @Test
    public void informationControlCreatorAlwaysProvidesDefaultControl() {
        final RedCompletionProposal proposal = new RedCompletionProposal("", 34, 0, 0, 56, 20, null, false, null, false,
                new ArrayList<Runnable>(), null, false);
        final IInformationControlCreator creator = proposal.getInformationControlCreator();

        final Shell shell = new Shell(Display.getCurrent());
        final IInformationControl infoControl = creator.createInformationControl(shell);
        assertThat(infoControl).isExactlyInstanceOf(DefaultInformationControl.class);

        shell.close();
        shell.dispose();
    }

    @Test
    public void informationControlCreatorIsNull_whenAdditionalInfoUsesHtml() {
        final RedCompletionProposal proposal = new RedCompletionProposal("", 34, 0, 0, 56, 20, null, false, null, false,
                new ArrayList<Runnable>(), null, true);

        assertThat(proposal.getInformationControlCreator()).isNull();
    }

    @Test
    public void contentInDocumentIsProperlyReplacedByProposal() {
        final IDocument document = new Document("document content is here");

        final RedCompletionProposal proposal = new RedCompletionProposal("stuff with suffix", 9, 7, 0, 0, 0, null,
                false, null, false, new ArrayList<Runnable>(), null, false);

        proposal.apply(document);
        assertThat(document).isEqualTo(new Document("document stuff with suffix is here"));
    }
}
