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
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class SettingsAssistProcessor extends RedContentAssistProcessor {

    private static final List<String> KW_SETTING_NAMES = newArrayList("[Arguments]", "[Documentation]", "[Return]",
            "[Tags]", "[Teardown]", "[Timeout]");

    private static final List<String> TC_SETTING_NAMES = newArrayList("[Documentation]", "[Setup]", "[Tags]",
            "[Teardown]", "[Template]", "[Timeout]");

    private final SuiteSourceAssistantContext assist;

    public SettingsAssistProcessor(final SuiteSourceAssistantContext assist) {
        this.assist = assist;
    }

    @Override
    protected List<String> getValidContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.TEST_CASES_SECTION,
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);
    }

    @Override
    protected String getProposalsTitle() {
        return "Test Case/Keyword settings";
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
                final String separator = assist.getSeparatorToFollow();

                final List<RedCompletionProposal> proposals = newArrayList();
                for (final String settingName : getSettingsToUse(document, offset)) {

                    if (settingName.toLowerCase().startsWith(prefix.toLowerCase())) {
                        final String textToInsert = settingName + separator;

                        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                                .will(assist.getAcceptanceMode())
                                .theText(textToInsert)
                                .atOffset(offset - prefix.length())
                                .givenThatCurrentPrefixIs(prefix)
                                .andWholeContentIs(content)
                                .thenCursorWillStopAtTheEndOfInsertion()
                                .currentPrefixShouldBeDecorated()
                                .displayedLabelShouldBe(textToInsert)
                                .proposalsShouldHaveIcon(ImagesManager.getImage(RedImages.getRobotSettingImage()))
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

    private List<String> getSettingsToUse(final IDocument document, final int offset) throws BadLocationException {
        return SuiteSourcePartitionScanner.TEST_CASES_SECTION.equals(getVirtualContentType(document, offset))
                ? TC_SETTING_NAMES : KW_SETTING_NAMES;
    }

    private boolean shouldShowProposals(final String lineContent, final IDocument document, final int offset)
            throws BadLocationException {
        return isInProperContentType(document, offset)
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) == 1;
    }
}
