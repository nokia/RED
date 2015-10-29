/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceAssistantContext;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.RedCompletionBuilder;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.RedCompletionProposal;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class VariablesAssistProcessor extends RedContentAssistProcessor {

    private final SuiteSourceAssistantContext assist;

    public VariablesAssistProcessor(final SuiteSourceAssistantContext assist) {
        this.assist = assist;
    }

    @Override
    protected String getProposalsTitle() {
        return "Variables";
    }

    @Override
    protected List<ICompletionProposal> computeProposals(final ITextViewer viewer, final int offset) {
        final IDocument document = viewer.getDocument();
        try {
            final String lineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document, offset);

            if (!shouldShowProposals(lineContent, document, offset)) {
                return null;
            }

            final Optional<IRegion> variable = DocumentUtilities.findLiveVariable(document, offset);

            final String prefix = variable.isPresent() ? getPrefix(document, variable.get(), offset) : "";
            final String content = variable.isPresent()
                    ? document.get(variable.get().getOffset(), variable.get().getLength()) : "";

            final List<ICompletionProposal> proposals = newArrayList();
            for (final RedVariableProposal varProposal : assist.getVariables(offset)) {
                if (varProposal.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                    final String additionalInfo = createSecondaryInfo(varProposal);
                    final Image image = getImage(varProposal.getName());

                    final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                            .will(assist.getAcceptanceMode())
                            .theText(varProposal.getName())
                            .atOffset(offset - prefix.length())
                            .givenThatCurrentPrefixIs(prefix)
                            .andWholeContentIs(content)
                            .secondaryPopupShouldBeDisplayed(additionalInfo)
                            .thenCursorWillStopAtTheEndOfInsertion()
                            .currentPrefixShouldBeDecorated()
                            .proposalsShouldHaveIcon(image)
                            .create();
                    proposals.add(proposal);
                }
            }
            return proposals;
        } catch (final BadLocationException e) {
            return newArrayList();
        }
    }

    private boolean shouldShowProposals(final String lineContent, final IDocument document, final int offset)
            throws BadLocationException {
        return isInProperContentType(offset, document) && DocumentUtilities.getNumberOfCellSeparators(lineContent) >= 1;
    }

    private boolean isInProperContentType(final int offset, final IDocument document) throws BadLocationException {
        // it is valid to show those proposals when we are in non-default content type or in default
        // type at the end of document when previous content type is non-default
        final String contentType = document.getContentType(offset);
        if (contentType == IDocument.DEFAULT_CONTENT_TYPE) {
            return offset > 0 && offset == document.getLength()
                    && document.getContentType(offset - 1) != IDocument.DEFAULT_CONTENT_TYPE;
        }
        return true;
    }

    private Image getImage(final String name) {
        if (name.startsWith("&")) {
            return ImagesManager.getImage(RedImages.getRobotDictionaryVariableImage());
        } else if (name.startsWith("@")) {
            return ImagesManager.getImage(RedImages.getRobotListVariableImage());
        } else {
            return ImagesManager.getImage(RedImages.getRobotScalarVariableImage());
        }
    }

    private static String createSecondaryInfo(final RedVariableProposal varProposal) {
        String info = "Source: " + varProposal.getSource() + "\n";
        final String value = varProposal.getValue();
        if (!value.isEmpty()) {
            info += "Value: " + value + "\n";
        }
        final String comment = varProposal.getComment();
        if (!comment.isEmpty()) {
            info += "Comment: " + comment;
        }
        return info;
    }

    private String getPrefix(final IDocument document, final IRegion wholeRegion, final int offset) {
        try {
            return document.get(wholeRegion.getOffset(), offset - wholeRegion.getOffset());
        } catch (final BadLocationException e) {
            return "";
        }
    }
}
