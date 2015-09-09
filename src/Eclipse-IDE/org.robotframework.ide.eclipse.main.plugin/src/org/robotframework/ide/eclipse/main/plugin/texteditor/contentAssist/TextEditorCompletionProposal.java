/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist;

import org.eclipse.core.runtime.Assert;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

public class TextEditorCompletionProposal implements ICompletionProposal, ICompletionProposalExtension3,
        ICompletionProposalExtension6 {

    /** The string to be displayed in the completion proposal popup. */
    private String displayString;

    /** The replacement string. */
    private String replacementString;

    /** The replacement offset. */
    private int replacementOffset;

    /** The replacement length. */
    private int replacementLength;

    /** The cursor position after this proposal has been applied. */
    private int cursorPosition;

    /** The image to be displayed in the completion proposal popup. */
    private Image image;

    /** The context information of this proposal. */
    private IContextInformation contextInformation;

    /** The additional info of this proposal. */
    private String additionalProposalInfo;

    private String sourceName;

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
     */
    public TextEditorCompletionProposal(String replacementString, int replacementOffset, int replacementLength,
            int cursorPosition, Image image, String displayString, IContextInformation contextInformation,
            String additionalProposalInfo, String sourceName) {
        Assert.isNotNull(replacementString);
        Assert.isTrue(replacementOffset >= 0);
        Assert.isTrue(replacementLength >= 0);
        Assert.isTrue(cursorPosition >= 0);

        this.replacementString = replacementString;
        this.replacementOffset = replacementOffset;
        this.replacementLength = replacementLength;
        this.cursorPosition = cursorPosition;
        this.image = image;
        this.displayString = displayString;
        this.contextInformation = contextInformation;
        this.additionalProposalInfo = additionalProposalInfo;
        this.sourceName = sourceName;
    }

    @Override
    public void apply(IDocument document) {
        try {
            document.replace(replacementOffset, replacementLength, replacementString);
        } catch (BadLocationException x) {
            // ignore
        }
    }

    @Override
    public Point getSelection(IDocument document) {
        return new Point(replacementOffset + cursorPosition, 0);
    }

    @Override
    public String getAdditionalProposalInfo() {
        return additionalProposalInfo;
    }

    @Override
    public String getDisplayString() {
        if (displayString != null)
            return displayString;
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
        return new IInformationControlCreator() {

            @Override
            public IInformationControl createInformationControl(Shell parent) {
                return new DefaultInformationControl(parent);
            }
        };
    }

    @Override
    public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
        return null;
    }

    @Override
    public int getPrefixCompletionStart(IDocument document, int completionOffset) {
        return 0;
    }

    @Override
    public StyledString getStyledDisplayString() {
        StyledString styledString = new StyledString();
        styledString.append(getDisplayString());
        if (sourceName != null) {
            styledString.append(" - " + sourceName, StyledString.DECORATIONS_STYLER);
        }

        return styledString;
    }

}
