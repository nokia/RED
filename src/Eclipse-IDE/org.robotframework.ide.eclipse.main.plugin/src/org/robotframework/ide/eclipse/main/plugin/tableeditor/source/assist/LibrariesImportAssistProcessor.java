/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class LibrariesImportAssistProcessor extends RedContentAssistProcessor {

    private final SuiteSourceAssistantContext assist;

    public LibrariesImportAssistProcessor(final SuiteSourceAssistantContext assist) {
        this.assist = assist;
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Override
    protected String getProposalsTitle() {
        return "Libraries";
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final ITextViewer viewer, final int offset) {
        final IDocument document = viewer.getDocument();
        try {
            final String lineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document, offset);
            final boolean shouldShowProposal = shouldShowProposals(lineContent, document, offset);

            if (shouldShowProposal) {
                final boolean isTsv = assist.isTsvFile();
                final Optional<IRegion> region = DocumentUtilities.findLiveCellRegion(document, isTsv, offset);
                final String prefix = DocumentUtilities.getPrefix(document, region, offset);
                final String content = region.isPresent()
                        ? document.get(region.get().getOffset(), region.get().getLength()) : "";
                final String separator = assist.getSeparatorToFollow();

                final List<RedCompletionProposal> proposals = newArrayList();
                for (final LibrarySpecification library : assist.getLibraries()) {
                    final String libraryName = library.getName();

                    if (libraryName.toLowerCase().startsWith(prefix.toLowerCase())) {

                        final String argument = library.isRemote() ? separator + library.getSecondaryKey() : "";
                        final String textToInsert = libraryName + argument;
                        final String labelToDisplay = libraryName
                                + (library.isRemote() ? " " + library.getSecondaryKey() : "");

                        final Image image = ImagesManager.getImage(RedImages.getLibraryImage());
                        
                        final boolean isImported = assist.isAlreadyImported(library);
                        final String importedInfo = isImported ? "(already imported)" : null;
                        
                        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                                .will(assist.getAcceptanceMode())
                                .theText(textToInsert)
                                .atOffset(offset - prefix.length())
                                .givenThatCurrentPrefixIs(prefix)
                                .andWholeContentIs(content)
                                .secondaryPopupShouldBeDisplayed(library.getDocumentation())
                                .thenCursorWillStopAtTheEndOfInsertion()
                                .currentPrefixShouldBeDecorated()
                                .displayedLabelShouldBe(labelToDisplay)
                                .proposalsShouldHaveIcon(image)
                                .labelShouldBeAugmentedWith(importedInfo)
                                .createWithPriority(isImported ? 1 : 0);
                        
                        proposals.add(proposal);
                    }
                }
                Collections.sort(proposals);
                return proposals;
            }
            return null;
        } catch (final BadLocationException e) {
            return null;
        }
    }

    private boolean shouldShowProposals(final String lineContent, final IDocument document, final int offset)
            throws BadLocationException {
        return isInApplicableContentType(document, offset) && lineContent.toLowerCase().startsWith("library")
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) == 1;
    }

}
