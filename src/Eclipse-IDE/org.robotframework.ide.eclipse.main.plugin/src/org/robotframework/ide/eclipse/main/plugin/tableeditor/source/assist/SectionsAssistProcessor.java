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
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSectionProposals;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentModification;

/**
 * @author Michal Anglart
 */
public class SectionsAssistProcessor extends RedContentAssistProcessor {

    public SectionsAssistProcessor(final AssistantContext assist) {
        super(assist);
    }

    @Override
    public String getProposalsTitle() {
        return "Sections";
    }

    @Override
    public List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION, SuiteSourcePartitionScanner.TASKS_SECTION,
                SuiteSourcePartitionScanner.SETTINGS_SECTION, SuiteSourcePartitionScanner.VARIABLES_SECTION);
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        return isInFirstCellOfTheLine(document, offset);
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final IDocument document, final int offset,
            final int cellLength, final String userContent, final boolean atTheEndOfLine) throws BadLocationException {

        final String contentSuffix = atTheEndOfLine ? DocumentUtilities.getDelimiter(document) : "";

        final List<? extends AssistProposal> sectionProposals = new RedSectionProposals()
                .getSectionsProposals(userContent);

        final List<ICompletionProposal> proposals = newArrayList();
        for (final AssistProposal settingProposal : sectionProposals) {
            final DocumentModification modification = new DocumentModification(contentSuffix,
                    new Position(offset - userContent.length(), cellLength));

            proposals.add(new RedCompletionProposalAdapter(assist, settingProposal, modification));
        }
        return proposals;
    }
}
