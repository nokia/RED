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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class ForLoopAssistProcessor extends RedContentAssistProcessor {

    private static final String FOR_LOOP_1 = ": FOR";
    private static final String FOR_LOOP_2 = ":FOR";
    private static final List<String> LOOP_ELEMENTS = newArrayList("IN", "IN ENUMERATE", "IN RANGE", "IN ZIP");

    private final SuiteSourceAssistantContext assist;

    public ForLoopAssistProcessor(final SuiteSourceAssistantContext assist) {
        this.assist = assist;
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.TEST_CASES_SECTION,
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);
    }

    @Override
    protected String getProposalsTitle() {
        return "For loop";
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

                if (DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) == 1) {
                    return getForProposal(prefix, content, offset);
                } else if (DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) > 2) {
                    return getInConstructsProposals(prefix, content, offset);
                } else {
                    return newArrayList();
                }
            }
            return null;
        } catch (final BadLocationException e) {
            return null;
        }
    }

    private List<? extends ICompletionProposal> getForProposal(final String prefix, final String content,
            final int offset) {
        if (FOR_LOOP_1.toLowerCase().startsWith(prefix.toLowerCase())
                || FOR_LOOP_2.toLowerCase().startsWith(prefix.toLowerCase())) {
            final String separator = getSeparatorToFollow();
            final String textToInsert = FOR_LOOP_1 + separator;

            final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                    .will(assist.getAcceptanceMode())
                    .theText(textToInsert)
                    .atOffset(offset - prefix.length())
                    .givenThatCurrentPrefixIs(prefix)
                    .andWholeContentIs(content)
                    .thenCursorWillStopAtTheEndOfInsertion()
                    .currentPrefixShouldBeDecorated()
                    .displayedLabelShouldBe(FOR_LOOP_1)
                    .create();
            return newArrayList(proposal);
        } else {
            return newArrayList();
        }
    }

    private List<? extends ICompletionProposal> getInConstructsProposals(final String prefix, final String content,
            final int offset) {
        final List<RedCompletionProposal> proposals = newArrayList();
        for (final String inProposal : LOOP_ELEMENTS) {
            if (inProposal.toLowerCase().startsWith(prefix)) {
                final String separator = getSeparatorToFollow();
                final String textToInsert = inProposal + separator;

                final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                        .will(assist.getAcceptanceMode())
                        .theText(textToInsert)
                        .atOffset(offset - prefix.length())
                        .givenThatCurrentPrefixIs(prefix)
                        .andWholeContentIs(content)
                        .thenCursorWillStopAtTheEndOfInsertion()
                        .currentPrefixShouldBeDecorated()
                        .displayedLabelShouldBe(inProposal)
                        .create();
                proposals.add(proposal);
            }
        }
        return proposals;
    }

    protected String getSeparatorToFollow() {
        return assist.getSeparatorToFollow();
    }

    protected boolean shouldShowProposals(final String lineContent, final IDocument document, final int offset)
            throws BadLocationException {
        return isInApplicableContentType(document, offset)
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) > 0;
    }

}
