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
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.RedCompletionBuilder;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.RedCompletionProposal;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class KeywordsInSettingsAssistProcessor extends RedContentAssistProcessor {

    private final SuiteSourceAssistantContext assist;

    public KeywordsInSettingsAssistProcessor(final SuiteSourceAssistantContext assist) {
        this.assist = assist;
    }

    @Override
    protected List<String> getValidContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Override
    protected String getProposalsTitle() {
        return "Keywords";
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final ITextViewer viewer, final int offset) {
        final IDocument document = viewer.getDocument();
        try {
            final String lineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document, offset);
            final boolean shouldShowProposal = shouldShowProposals(lineContent, document, offset);

            if (shouldShowProposal) {
                final Optional<IRegion> region = DocumentUtilities.findLiveCellRegion(document, offset);
                final String prefix = getPrefix(document, region, offset);
                final String content = region.isPresent()
                        ? document.get(region.get().getOffset(), region.get().getLength()) : "";

                final List<RedCompletionProposal> proposals = newArrayList();
                for (final RedKeywordProposal keywordProposal : assist.getKeywords()) {
                    final String keywordName = keywordProposal.getLabel();

                    if (keywordName.toLowerCase().startsWith(prefix.toLowerCase())) {
                        final String textToInsert = keywordName;

                        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                                .will(assist.getAcceptanceMode())
                                .theText(textToInsert)
                                .atOffset(offset - prefix.length())
                                .givenThatCurrentPrefixIs(prefix)
                                .andWholeContentIs(content)
                                .secondaryPopupShouldBeDisplayed(keywordProposal.getDocumentation())
                                .thenCursorWillStopAtTheEndOfInsertion()
                                .currentPrefixShouldBeDecorated()
                                .displayedLabelShouldBe(textToInsert)
                                .labelShouldBeAugmentedWith(keywordProposal.getLabelDecoration())
                                .proposalsShouldHaveIcon(ImagesManager.getImage(keywordProposal.getImage()))
                                .create();

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
        return isInProperContentType(document, offset) && DocumentUtilities.getNumberOfCellSeparators(lineContent) == 1
                && (lineContent.toLowerCase().startsWith("suite setup")
                || lineContent.toLowerCase().startsWith("suite teardown")
                || lineContent.toLowerCase().startsWith("test setup")
                || lineContent.toLowerCase().startsWith("test teardown")
                || lineContent.toLowerCase().startsWith("test template"));
    }

    private String getPrefix(final IDocument document, final Optional<IRegion> optional, final int offset) {
        if (!optional.isPresent()) {
            return "";
        }
        try {
            return document.get(optional.get().getOffset(), offset - optional.get().getOffset());
        } catch (final BadLocationException e) {
            return "";
        }
    }
}
