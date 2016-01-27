/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;


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

                final List<RedKeywordProposal> matchingProposals = getMatchingProposals(prefix);
                final Multimap<GroupKey, RedKeywordProposal> groupedProposals = groupProposalsByContent(
                        matchingProposals);

                for (final RedKeywordProposal keywordProposal : matchingProposals) {
                    final String keywordName = keywordProposal.getContent();
                    final boolean shouldAddKeywordPrefix = (isKeywordPrefixAutoAdditionEnabled || keywordProposalIsConflicting(
                            groupedProposals, keywordProposal)) && keywordProposal.getScope() != KeywordScope.LOCAL;
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

    private List<RedKeywordProposal> getMatchingProposals(final String prefix) {
        final List<RedKeywordProposal> proposals = new ArrayList<>();
        for (final RedKeywordProposal keywordProposal : assist.getKeywords()) {
            final String keywordName = keywordProposal.getLabel();
            final String keywordPrefix = keywordProposal.getSourcePrefix() + ".";
            final String wholeDefinition = keywordPrefix + keywordName;

            if (EmbeddedKeywordNamesSupport.startsWith(keywordName, prefix)
                    || EmbeddedKeywordNamesSupport.startsWith(wholeDefinition, prefix)) {
                proposals.add(keywordProposal);
            }
        }
        return proposals;
    }

    private Multimap<GroupKey, RedKeywordProposal> groupProposalsByContent(final List<RedKeywordProposal> proposals) {
        final Multimap<GroupKey, RedKeywordProposal> groupedProposals = LinkedHashMultimap.create();
        for (final RedKeywordProposal proposal : proposals) {
            groupedProposals.put(new GroupKey(proposal.getScope(), proposal.getContent()), proposal);
        }
        return groupedProposals;
    }

    private boolean keywordProposalIsConflicting(final Multimap<GroupKey, RedKeywordProposal> groupedProposals,
            final RedKeywordProposal proposal) {
        return groupedProposals.get(new GroupKey(proposal.getScope(), proposal.getContent())).size() > 1;
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

    private class GroupKey {

        private final Object[] groupingKeys;

        public GroupKey(final Object... groupingKeys) {
            this.groupingKeys = groupingKeys;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof GroupKey) {
                final GroupKey that = (GroupKey) obj;
                return Objects.equal(this.groupingKeys, that.groupingKeys);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(groupingKeys);
        }

    }
}
