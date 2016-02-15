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
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.swt.SwtThread;

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
                    if (isReserved(keywordProposal)) {
                        continue;
                    }

                    final String keywordName = keywordProposal.getContent();
                    final boolean shouldAddKeywordPrefix = (isKeywordPrefixAutoAdditionEnabled || keywordProposalIsConflicting(keywordProposal))
                            && keywordProposal.getScope(assist.getFile().getFullPath()) != KeywordScope.LOCAL; // Local keywords can't have prefix
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
                            .performAfterAccepting(createOperationsToPerformAfterAccepting(
                                    calculateRegionsForLinkedMode(offset - prefix.length(), keywordName), viewer))
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

    private Collection<IRegion> calculateRegionsForLinkedMode(final int beginOffset, final String keywordName) {
        final Collection<IRegion> regions = new ArrayList<>();
        if (EmbeddedKeywordNamesSupport.hasEmbeddedArguments(keywordName)) {
            final Matcher matcher = Pattern.compile("\\$\\{[^\\}]+\\}").matcher(keywordName);
            while (matcher.find()) {
                regions.add(new Region(beginOffset + matcher.start(), matcher.end() - matcher.start()));
            }
        }
        return regions;
    }

    private Collection<Runnable> createOperationsToPerformAfterAccepting(final Collection<IRegion> regionsToLinkedEdit,
            final ITextViewer viewer) {
        final List<Runnable> operations = new ArrayList<>();
        if (!regionsToLinkedEdit.isEmpty()) {
            operations.add(new Runnable() {
                @Override
                public void run() {
                    SwtThread.asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                final LinkedModeModel model = new LinkedModeModel();
                                for (final IRegion region : regionsToLinkedEdit) {
                                    final LinkedPositionGroup group = new LinkedPositionGroup();
                                    group.addPosition(new LinkedPosition(viewer.getDocument(), region.getOffset(),
                                            region.getLength()));
                                    model.addGroup(group);
                                }
                                model.forceInstall();
                                final LinkedModeUI ui = new LinkedModeUI(model, new ITextViewer[] { viewer });
                                ui.enter();
                            } catch (final BadLocationException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }
        return operations;
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
        return isInProperContentType(document, offset)
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) > 0;
    }
}
