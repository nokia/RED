/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals.sortedByNames;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class KeywordCallsAssistProcessor extends RedContentAssistProcessor {

    protected final SuiteSourceAssistantContext assist;

    public KeywordCallsAssistProcessor(final SuiteSourceAssistantContext assist) {
        this.assist = assist;
    }

    @Override
    protected List<String> getValidContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.TEST_CASES_SECTION,
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);
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
                final String prefix = DocumentUtilities.getPrefix(document, region, offset);
                final String content = region.isPresent()
                        ? document.get(region.get().getOffset(), region.get().getLength()) : "";
                final String separator = getSeparatorToFollow();
                final boolean isKeywordPrefixAutoAdditionEnabled = isKeywordPrefixAutoAdditionEnabled();

                final List<RedCompletionProposal> proposals = newArrayList();

                for (final RedKeywordProposal keywordProposal : assist.getKeywords(prefix, sortedByNames())) {
                    final String keywordName = keywordProposal.getContent();
                    final boolean shouldAddKeywordPrefix = (isKeywordPrefixAutoAdditionEnabled
                            || keywordProposalIsConflicting(keywordProposal));
                    final String keywordPrefix = shouldAddKeywordPrefix ? keywordProposal.getSourcePrefix() + "." : "";
                    final String textToInsert = keywordPrefix + keywordName + separator;

                    final List<String> args = keywordProposal.getArguments();
                    final String arguments = args.isEmpty() ? "no arguments" : Joiner.on(" | ").join(args);
                    final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                            .will(assist.getAcceptanceMode())
                            .theText(textToInsert)
                            .atOffset(offset - prefix.length())
                            .givenThatCurrentPrefixIs(
                                    prefix.contains(".") ? prefix.substring(prefix.lastIndexOf('.') + 1) : prefix)
                            .andWholeContentIs(content)
                            .secondaryPopupShouldBeDisplayed(keywordProposal.getDocumentation())
                            .contextInformationShouldBeShownAfterAccepting(
                                    new ContextInformation(keywordName, arguments))
                            .thenCursorWillStopAtTheEndOfInsertion()
                            .currentPrefixShouldBeDecorated()
                            .displayedLabelShouldBe(keywordName)
                            .andItShouldBeStrikedout(keywordProposal.isDeprecated())
                            .labelShouldBeAugmentedWith(keywordProposal.getLabelDecoration())
                            .proposalsShouldHaveIcon(ImagesManager.getImage(keywordProposal.getImage()))
                            .create();
                    proposals.add(proposal);
                }
                Collections.sort(proposals);
                return proposals;
            }
            return null;
        } catch (final BadLocationException e) {
            return null;
        }
    }

    private boolean keywordProposalIsConflicting(final RedKeywordProposal keywordEntity) {
        final KeywordEntity bestMatching = assist.getBestMatchingKeyword(keywordEntity.getNameFromDefinition());
        return !bestMatching.equals(keywordEntity);
    }

    protected String getSeparatorToFollow() {
        return assist.getSeparatorToFollow();
    }
    
    protected boolean isKeywordPrefixAutoAdditionEnabled() {
        return assist.isKeywordPrefixAutoAdditionEnabled();
    }

    protected boolean shouldShowProposals(final String lineContent, final IDocument document, final int offset)
            throws BadLocationException {
        return isInProperContentType(document, offset)
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) > 0;
    }
}
