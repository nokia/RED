/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import com.google.common.base.Preconditions;

public class RedCompletionProposal implements Comparable<RedCompletionProposal>, ICompletionProposal,
        ICompletionProposalExtension3, ICompletionProposalExtension6 {

    /** The string to be displayed in the completion proposal popup. */
    private final String displayString;

    private final boolean strikeout;

    /** The replacement string. */
    private final String replacementString;

    /** The replacement offset. */
    private final int replacementOffset;

    /** The replacement length. */
    private final int replacementLength;

    private final int prefixLength;

    /** The cursor position after this proposal has been applied. */
    private final int cursorPosition;

    private final int selectionLength;

    /** The image to be displayed in the completion proposal popup. */
    private final Image image;

    /** The context information of this proposal. */
    private final IContextInformation contextInformation;

    /** The additional info of this proposal. */
    private final String additionalProposalInfo;

    private final boolean additionalInfoAsHtml;

    private final String additionalInfoForStyledLabel;

    private final boolean decoratePrefix;

    private final int priority;

    private final boolean activateAssistant;

    /**
     * Creates a new completion proposal. All fields are initialized based on the provided
     * information.
     *
     * @param replacementString
     *            the actual string to be inserted into the document
     * @param replacementOffset
     *            the offset of the text to be replaced
     * @param replacementLength
     *            the length of the text to be replaced
     * @param cursorPosition
     *            the position of the cursor following the insert relative to replacementOffset
     * @param image
     *            the image to display for this proposal
     * @param displayString
     *            the string to be displayed for the proposal
     * @param contextInformation
     *            the context information associated with this proposal
     * @param additionalProposalInfo
     *            the additional information associated with this proposal
     * @param additionalInfoForStyledLabel
     *            the additional information visible as styled label part
     * @param prefixLength
     * @param decoratePrefix
     * @param activateAssitant
     * @param additionalInfoAsHtml
     */
    RedCompletionProposal(final int priority, final String replacementString, final int replacementOffset,
            final int replacementLength, final int prefixLength, final int cursorPosition, final int selectionLength,
            final Image image, final boolean decoratePrefix, final String displayString, final boolean activateAssitant,
            final IContextInformation contextInformation, final String additionalProposalInfo,
            final boolean additionalInfoAsHtml, final String additionalInfoForStyledLabel, final boolean strikeout) {
        Preconditions.checkNotNull(replacementString);
        Preconditions.checkState(replacementOffset >= 0);
        Preconditions.checkState(replacementLength >= 0);
        Preconditions.checkState(cursorPosition >= 0);

        this.priority = priority;
        this.replacementString = replacementString;
        this.replacementOffset = replacementOffset;
        this.replacementLength = replacementLength;
        this.prefixLength = prefixLength;
        this.decoratePrefix = decoratePrefix;
        this.cursorPosition = cursorPosition;
        this.selectionLength = selectionLength;
        this.image = image;
        this.displayString = displayString;
        this.strikeout = strikeout;
        this.contextInformation = contextInformation;
        this.activateAssistant = activateAssitant;
        this.additionalProposalInfo = additionalProposalInfo;
        this.additionalInfoAsHtml = additionalInfoAsHtml;
        this.additionalInfoForStyledLabel = additionalInfoForStyledLabel;
    }

    @Override
    public int compareTo(final RedCompletionProposal that) {
        if (this.priority == that.priority) {
            return this.getDisplayString().compareTo(that.getDisplayString());
        }
        return Integer.valueOf(this.priority).compareTo(Integer.valueOf(that.priority));
    }

    @Override
    public void apply(final IDocument document) {
        try {
            document.replace(replacementOffset, replacementLength, replacementString);
        } catch (final BadLocationException x) {
            // ignore
        }
    }

    @Override
    public Point getSelection(final IDocument document) {
        return new Point(replacementOffset + cursorPosition, selectionLength);
    }

    @Override
    public String getAdditionalProposalInfo() {
        return additionalProposalInfo;
    }

    @Override
    public String getDisplayString() {
        if (displayString != null) {
            return displayString;
        }
        return replacementString;
    }

    @Override
    public Image getImage() {
        return image;
    }

    @Override
    public IContextInformation getContextInformation() {
        return contextInformation;
    }

    @Override
    public IInformationControlCreator getInformationControlCreator() {
        return additionalInfoAsHtml ? null : new IInformationControlCreator() {
            @Override
            public IInformationControl createInformationControl(final Shell parent) {
                return new DefaultInformationControl(parent);
            }
        };
    }

    @Override
    public CharSequence getPrefixCompletionText(final IDocument document, final int completionOffset) {
        return replacementString;
    }

    @Override
    public int getPrefixCompletionStart(final IDocument document, final int completionOffset) {
        return replacementOffset;
    }

    @Override
    public StyledString getStyledDisplayString() {
        final StyledString styledString = new StyledString();
        final String toDisplay = getDisplayString();

        if (decoratePrefix) {
            final String alreadyWrittenPrefix = toDisplay.substring(0, prefixLength);
            final String suffixWhichWillBeAdded = toDisplay.substring(prefixLength);
            styledString.append(alreadyWrittenPrefix,
                    strikeout ? Stylers.Common.MARKED_STRIKEOUT_PREFIX_STYLER : Stylers.Common.MARKED_PREFIX_STYLER);
            styledString.append(suffixWhichWillBeAdded,
                    strikeout ? Stylers.Common.STRIKEOUT_STYLER : Stylers.Common.EMPTY_STYLER);
        } else {
            styledString.append(toDisplay, strikeout ? Stylers.Common.STRIKEOUT_STYLER : Stylers.Common.EMPTY_STYLER);
        }
        if (additionalInfoForStyledLabel != null) {
            styledString.append(" " + additionalInfoForStyledLabel, StyledString.DECORATIONS_STYLER);
        }
        return styledString;
    }

    public boolean shouldActivateAssitantAfterAccepting() {
        return activateAssistant;
    }

}
