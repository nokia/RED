/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals.SettingTarget;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentModification;

/**
 * @author Michal Anglart
 */
public class GeneralSettingsAssistProcessor extends RedContentAssistProcessor {

    public GeneralSettingsAssistProcessor(final SuiteSourceAssistantContext assist) {
        super(assist);
    }

    @Override
    protected String getProposalsTitle() {
        return "Settings";
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        if (isInApplicableContentType(document, offset)) {
            final IRegion lineInfo = document.getLineInformationOfOffset(offset);
            if (offset != lineInfo.getOffset()) {
                final Optional<IRegion> cellRegion = DocumentUtilities.findLiveCellRegion(document, assist.isTsvFile(),
                        offset);
                return cellRegion.isPresent() && lineInfo.getOffset() == cellRegion.get().getOffset();
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final IDocument document, final int offset,
            final int cellLength, final String userContent, final boolean atTheEndOfLine) throws BadLocationException {

        final String contentSuffix = atTheEndOfLine ? assist.getSeparatorToFollow() : "";

        final List<? extends AssistProposal> settingsProposals = new RedSettingProposals(SettingTarget.GENERAL)
                .getSettingsProposals(userContent);

        final List<ICompletionProposal> proposals = newArrayList();
        for (final AssistProposal settingProposal : settingsProposals) {
            final DocumentModification modification = new DocumentModification(contentSuffix,
                    new Position(offset - userContent.length(), cellLength),
                    shouldActivate(settingProposal.getContent()));

            proposals.add(new RedCompletionProposalAdapter(settingProposal, modification));
        }
        return proposals;
    }

    private boolean shouldActivate(final String settingName) {
        return newArrayList("library", "resource", "variables", "test setup", "test teardown", "suite setup",
                "suite teardown", "test template").contains(settingName.toLowerCase());
    }
}
