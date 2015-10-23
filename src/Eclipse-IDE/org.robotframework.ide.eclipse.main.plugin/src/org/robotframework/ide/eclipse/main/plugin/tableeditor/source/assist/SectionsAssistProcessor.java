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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.RedCompletionBuilder;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.RedCompletionProposal;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class SectionsAssistProcessor extends RedContentAssistProcessor {

    private static List<String> SECTION_NAMES = newArrayList("*** Keywords ***", "*** Settings ***",
            "*** Test Cases ***", "*** Variables ***");

    private final AssistPreferences assistPreferences = new AssistPreferences();

    @Override
    protected String getProposalsTitle() {
        return "Sections";
    }

    @Override
    public List<? extends ICompletionProposal> computeProposals(final ITextViewer viewer, final int offset) {
        final IDocument document = viewer.getDocument();
        try {
            final IRegion lineInformation = document.getLineInformationOfOffset(offset);
            final boolean shouldShowProposal = shouldShowProposals(offset, document, lineInformation);

            if (shouldShowProposal) {
                final String prefix = getPrefix(document, lineInformation, offset);
                final Optional<IRegion> cellRegion = DocumentUtilities.findCellRegion(document, offset);
                final String content = cellRegion.isPresent()
                        ? document.get(cellRegion.get().getOffset(), cellRegion.get().getLength()) : "";

                final List<ICompletionProposal> proposals = newArrayList();
                final Image image = ImagesManager.getImage(RedImages.getRobotCasesFileSectionImage());
                for (final String sectionName : SECTION_NAMES) {
                    if (sectionName.toLowerCase().startsWith(prefix.toLowerCase())) {
                        final String textToInsert = sectionName + DocumentUtilities.getDelimiter(document);
                        
                        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                                .will(assistPreferences.getAcceptanceMode())
                                .theText(textToInsert)
                                .atOffset(lineInformation.getOffset())
                                .givenThatCurrentPrefixIs(prefix)
                                .andWholeContentIs(content)
                                .thenCursorWillStopAtTheEndOfInsertion()
                                .currentPrefixShouldBeDecorated()
                                .displayedLabelShouldBe(sectionName)
                                .proposalsShouldHaveIcon(image)
                                .create();
                        proposals.add(proposal);
                    }
                }
                return proposals;

            }
            return null;
        } catch (final BadLocationException e) {
            return null;
        }
    }

    private boolean shouldShowProposals(final int offset, final IDocument document, final IRegion lineInformation)
            throws BadLocationException {
        if (offset != lineInformation.getOffset()) {
            final Optional<IRegion> cellRegion = DocumentUtilities.findLiveCellRegion(document, offset);
            return cellRegion.isPresent() && lineInformation.getOffset() == cellRegion.get().getOffset();
        }
        return true;
    }

    private String getPrefix(final IDocument document, final IRegion wholeRegion, final int offset) {
        try {
            return document.get(wholeRegion.getOffset(), offset - wholeRegion.getOffset());
        } catch (final BadLocationException e) {
            return "";
        }
    }
}
