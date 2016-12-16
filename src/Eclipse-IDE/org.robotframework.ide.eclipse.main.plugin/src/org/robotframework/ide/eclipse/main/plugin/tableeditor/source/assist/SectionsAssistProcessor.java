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
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSectionProposals;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentationModification;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class SectionsAssistProcessor extends RedContentAssistProcessor {

    public SectionsAssistProcessor(final SuiteSourceAssistantContext assist) {
        super(assist);
    }

    @Override
    protected String getProposalsTitle() {
        return "Sections";
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION,
                SuiteSourcePartitionScanner.SETTINGS_SECTION,
                SuiteSourcePartitionScanner.VARIABLES_SECTION);
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        final IRegion lineInfo = document.getLineInformationOfOffset(offset);
        if (offset != lineInfo.getOffset()) {
            final Optional<IRegion> cellRegion = DocumentUtilities.findLiveCellRegion(document, assist.isTsvFile(),
                    offset);
            return cellRegion.isPresent() && lineInfo.getOffset() == cellRegion.get().getOffset();
        }
        return true;
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final IDocument document, final int offset,
            final int cellLength, final String prefix) {

        final List<? extends AssistProposal> sectionProposals = new RedSectionProposals().getSectionsProposals(prefix);

        final List<ICompletionProposal> proposals = newArrayList();
        for (final AssistProposal settingProposal : sectionProposals) {
            final DocumentationModification modification = new DocumentationModification(
                    DocumentUtilities.getDelimiter(document),
                    assist.getAcceptanceMode().positionToReplace(offset, prefix.length(), cellLength));

            proposals.add(new RedCompletionProposalAdapter(settingProposal, modification));
        }
        return proposals;
    }
}
