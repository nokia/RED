/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class SectionsAssistProcessor extends RedContentAssistProcessor {

    private static final List<String> SECTION_NAMES = newArrayList("*** Keywords ***", "*** Settings ***",
            "*** Test Cases ***", "*** Variables ***");

    private final SuiteSourceAssistantContext assist;

    public SectionsAssistProcessor(final SuiteSourceAssistantContext assist) {
        this.assist = assist;
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION, SuiteSourcePartitionScanner.SETTINGS_SECTION,
                SuiteSourcePartitionScanner.VARIABLES_SECTION);
    }

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
                final String prefix = DocumentUtilities.getPrefix(document, Optional.of(lineInformation), offset);
                final Optional<IRegion> cellRegion = DocumentUtilities.findCellRegion(document, offset);
                final String content = cellRegion.isPresent()
                        ? document.get(cellRegion.get().getOffset(), cellRegion.get().getLength()) : "";

                final List<ICompletionProposal> proposals = newArrayList();
                final Image image = ImagesManager.getImage(RedImages.getRobotCasesFileSectionImage());
                for (final String sectionName : getPossibleSections()) {
                    if (sectionName.toLowerCase().startsWith(prefix.toLowerCase())) {
                        final String textToInsert = sectionName + DocumentUtilities.getDelimiter(document);
                        
                        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                                .will(assist.getAcceptanceMode())
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

    private List<String> getPossibleSections() {
        if (assist.getModel().isSuiteFile()) {
            return SECTION_NAMES;
        } else {
            final ArrayList<String> names = newArrayList(SECTION_NAMES);
            names.remove("*** Test Cases ***");
            return names;
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
}
