/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals.sortedByNames;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.text.link.RedEditorLinkedModeUI;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Range;


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
    protected List<String> getApplicableContentTypes() {
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
                final boolean isTsv = assist.isTsvFile();
                final Optional<IRegion> region = DocumentUtilities.findLiveCellRegion(document, isTsv, offset);
                final String prefix = GherkinStyleSupport
                        .getTextAfterGherkinPrefixIfExists(DocumentUtilities.getPrefix(document, region, offset));
                final String content = region.isPresent()
                        ? document.get(region.get().getOffset(), region.get().getLength()) : "";
                final String separator = getSeparatorToFollow();
                final boolean isKeywordPrefixAutoAdditionEnabled = isKeywordPrefixAutoAdditionEnabled();

                final List<RedCompletionProposal> proposals = newArrayList();

                for (final RedKeywordProposal keywordProposal : assist.getKeywords(prefix, sortedByNames())) {
                    if (isReserved(keywordProposal)) {
                        continue;
                    }

                    final String keywordName = keywordProposal.getContent();
                    final boolean shouldAddKeywordPrefix = keywordIsNotInLocalScope(keywordProposal)
                            && (isKeywordPrefixAutoAdditionEnabled || keywordProposalIsConflicting(keywordProposal));

                    final String keywordPrefix = shouldAddKeywordPrefix ? keywordProposal.getSourcePrefix() + "." : "";

                    String textToInsert;
                    final Collection<IRegion> regionsForLinkedMode;
                    if (EmbeddedKeywordNamesSupport.hasEmbeddedArguments(keywordName)) {
                        textToInsert = keywordPrefix + keywordName;
                        regionsForLinkedMode = calculateRegionsForLinkedModeForEmbeddedKeyword(
                                offset - prefix.length() + keywordPrefix.length(), keywordName);
                    } else {
                        final List<String> requiredArguments = getRequiredArguments(lineContent, keywordProposal);
                        final boolean addPlaceForOptional = shouldAddPlaceForOptionalArguments(lineContent,
                                keywordProposal, requiredArguments);

                        final String argumentsToInsert = requiredArguments.isEmpty() ? ""
                                : separator + Joiner.on(separator).join(requiredArguments);
                        textToInsert = keywordPrefix + keywordName + argumentsToInsert
                                + (addPlaceForOptional ? separator : "");
                        regionsForLinkedMode = calculateRegionsForLinkedModeForRegularKeyword(
                                offset - prefix.length() + keywordPrefix.length(), separator.length(), keywordName,
                                requiredArguments);
                    }

                    final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                            .will(assist.getAcceptanceMode())
                            .theText(textToInsert)
                            .atOffset(offset - prefix.length())
                            .givenThatCurrentPrefixIs(
                                    prefix.contains(".") ? prefix.substring(prefix.lastIndexOf('.') + 1) : prefix)
                            .andWholeContentIs(content)
                            .secondaryPopupShouldBeDisplayed(keywordProposal.getDocumentation())
                            .contextInformationShouldBeShownAfterAccepting(
                                    new ContextInformation(keywordName, keywordProposal.getArgumentsLabel()))
                            .performAfterAccepting(
                                    createOperationsToPerformAfterAccepting(viewer, regionsForLinkedMode))
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

    protected boolean shouldAddPlaceForOptionalArguments(final String lineContent,
            final RedKeywordProposal keywordProposal, final List<String> requiredArguments) {
        final Range<Integer> noOfArgs = keywordProposal.getNumberOfArguments();
        return !noOfArgs.hasUpperBound() || noOfArgs.upperEndpoint() > requiredArguments.size();
    }

    protected List<String> getRequiredArguments(final String lineContent, final RedKeywordProposal keywordProposal) {
        return keywordProposal.getRequiredArguments();
    }

    private boolean keywordIsNotInLocalScope(final RedKeywordProposal keywordProposal) {
        return keywordProposal
                .getScope(assist.getFile().getFullPath()) != KeywordScope.LOCAL;
    }

    private Collection<IRegion> calculateRegionsForLinkedModeForEmbeddedKeyword(final int beginOffset,
            final String keywordName) {
        final Collection<IRegion> regions = new ArrayList<>();
        final Matcher matcher = Pattern.compile("\\$\\{[^\\}]+\\}").matcher(keywordName);
        while (matcher.find()) {
            regions.add(new Region(beginOffset + matcher.start(), matcher.end() - matcher.start()));
        }
        return regions;
    }

    private Collection<IRegion> calculateRegionsForLinkedModeForRegularKeyword(final int beginOffset,
            final int separatorLength, final String keywordName, final List<String> requiredArguments) {
        final Collection<IRegion> regions = new ArrayList<>();
        int currentOffset = beginOffset + keywordName.length();
        if (!requiredArguments.isEmpty()) {
            currentOffset += separatorLength;
        }
        for (final String requiredArg : requiredArguments) {
            regions.add(new Region(currentOffset, requiredArg.length()));
            currentOffset += requiredArg.length() + separatorLength;
        }
        if (!requiredArguments.isEmpty()) {
            regions.add(new Region(currentOffset, 0));
        }
        return regions;
    }

    private Collection<Runnable> createOperationsToPerformAfterAccepting(final ITextViewer viewer,
            final Collection<IRegion> regionsToLinkedEdit) {
        if (regionsToLinkedEdit.isEmpty()) {
            return new ArrayList<>();
        }
        final Runnable operation = new Runnable() {
            @Override
            public void run() {
                SwtThread.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        RedEditorLinkedModeUI.enableLinkedMode(viewer, regionsToLinkedEdit);
                    }
                });
            }
        };
        return newArrayList(operation);
    }

    private boolean isReserved(final RedKeywordProposal keywordProposal) {
        return keywordProposal.getScope(assist.getFile().getFullPath()) == KeywordScope.STD_LIBRARY
                && keywordProposal.getSourceName().equals("Reserved");
    }

    private boolean keywordProposalIsConflicting(final RedKeywordProposal keywordEntity) {
        final KeywordEntity bestMatching = assist.getBestMatchingKeyword(keywordEntity.getNameFromDefinition());
        return bestMatching != null && !keywordEntity.equals(bestMatching);
    }

    protected String getSeparatorToFollow() {
        return assist.getSeparatorToFollow();
    }
    
    protected boolean isKeywordPrefixAutoAdditionEnabled() {
        return assist.isKeywordPrefixAutoAdditionEnabled();
    }

    protected boolean shouldShowProposals(final String lineContent, final IDocument document, final int offset)
            throws BadLocationException {
        return isInApplicableContentType(document, offset)
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) > 0;
    }
}
