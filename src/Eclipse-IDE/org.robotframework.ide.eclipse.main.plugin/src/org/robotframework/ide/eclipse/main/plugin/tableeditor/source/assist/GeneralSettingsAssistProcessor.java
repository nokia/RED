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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class GeneralSettingsAssistProcessor extends RedContentAssistProcessor {

    private static final List<String> SETTING_NAMES = newArrayList("Library", "Resource", "Variables", "Documentation",
            "Metadata", "Suite Setup", "Suite Teardown", "Force Tags", "Default Tags", "Test Setup", "Test Teardown",
            "Test Template", "Test Timeout");

    private final SuiteSourceAssistantContext assist;

    public GeneralSettingsAssistProcessor(final SuiteSourceAssistantContext assist) {
        this.assist = assist;
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Override
    protected String getProposalsTitle() {
        return "Settings";
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
                final String separator = assist.getSeparatorToFollow();
                for (final String settingName : SETTING_NAMES) {
                    if (settingName.toLowerCase().startsWith(prefix.toLowerCase())) {
                        final String textToInsert = settingName + separator;
                        final Image image = ImagesManager.getImage(RedImages.getRobotSettingImage());
                        
                        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                                .will(assist.getAcceptanceMode())
                                .theText(textToInsert)
                                .atOffset(lineInformation.getOffset())
                                .givenThatCurrentPrefixIs(prefix)
                                .andWholeContentIs(content)
                                .activateAssistantAfterAccepting(shouldActivate(settingName))
                                .thenCursorWillStopAtTheEndOfInsertion()
                                .currentPrefixShouldBeDecorated()
                                .displayedLabelShouldBe(settingName)
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

    private boolean shouldActivate(final String settingName) {
        return newArrayList("library", "resource", "variables", "test setup", "test teardown", "suite setup",
                "suite teardown", "test template").contains(settingName.toLowerCase());
    }

    private boolean shouldShowProposals(final int offset, final IDocument document, final IRegion lineInformation)
            throws BadLocationException {
        if (isInApplicableContentType(document, offset)) {
            if (offset != lineInformation.getOffset()) {
                final Optional<IRegion> cellRegion = DocumentUtilities.findLiveCellRegion(document, offset);
                return cellRegion.isPresent() && lineInformation.getOffset() == cellRegion.get().getOffset();
            } else {
                return true;
            }
        }
        return false;
    }
}
