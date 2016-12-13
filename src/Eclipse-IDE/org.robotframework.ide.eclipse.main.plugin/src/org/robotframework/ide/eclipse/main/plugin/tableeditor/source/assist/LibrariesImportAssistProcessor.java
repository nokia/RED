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
import org.robotframework.ide.eclipse.main.plugin.assist.RedLibraryProposals;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentationModification;

import com.google.common.base.Joiner;


/**
 * @author Michal Anglart
 *
 */
public class LibrariesImportAssistProcessor extends RedContentAssistProcessor {

    public LibrariesImportAssistProcessor(final SuiteSourceAssistantContext assist) {
        super(assist);
    }

    @Override
    protected String getProposalsTitle() {
        return "Libraries";
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        return isInApplicableContentType(document, offset) && lineContent.toLowerCase().startsWith("library")
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) == 1;
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final IDocument document, final int offset,
            final int cellLength, final String prefix) throws BadLocationException {

        final List<? extends AssistProposal> librariesProposals = new RedLibraryProposals(assist.getModel())
                .getLibrariesProposals(prefix);
        final String separator = assist.getSeparatorToFollow();

        final List<ICompletionProposal> proposals = newArrayList();
        for (final AssistProposal libProposal : librariesProposals) {
            final Position positionToReplace = assist.getAcceptanceMode().positionToReplace(offset, prefix.length(),
                    cellLength);

            final List<String> args = libProposal.getArguments();
            final String contentSuffix = separator + (args.isEmpty() ? "" : Joiner.on(separator).join(args));
            final DocumentationModification modification = new DocumentationModification(contentSuffix,
                    positionToReplace);

            proposals.add(new RedCompletionProposalAdapter(libProposal, modification));
        }
        return proposals;
    }
}
