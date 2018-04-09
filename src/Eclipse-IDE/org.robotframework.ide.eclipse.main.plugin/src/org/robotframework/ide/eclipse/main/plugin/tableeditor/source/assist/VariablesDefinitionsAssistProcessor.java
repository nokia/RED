/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedNewVariableProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedNewVariableProposals;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentModification;
import org.robotframework.red.jface.text.link.RedEditorLinkedModeUI;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Splitter;

/**
 * @author Michal Anglart
 */
public class VariablesDefinitionsAssistProcessor extends RedContentAssistProcessor {

    public VariablesDefinitionsAssistProcessor(final SuiteSourceAssistantContext assist) {
        super(assist);
    }

    @Override
    protected String getProposalsTitle() {
        return "Variable definitions";
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.VARIABLES_SECTION);
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        final IRegion lineInfo = document.getLineInformationOfOffset(offset);
        if (isInApplicableContentType(document, offset)) {
            // we only want to show those proposals in first cell of the line
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

        final List<RedNewVariableProposal> newVariableProposals = new RedNewVariableProposals()
                .getNewVariableProposals();

        final String separator = assist.getSeparatorToFollow();
        final List<ICompletionProposal> proposals = new ArrayList<>();

        final IRegion lineInfo = document.getLineInformationOfOffset(offset);
        for (final RedNewVariableProposal newVarProposal : newVariableProposals) {
            final List<String> args = newVarProposal.getArguments();
            final String contentSuffix = atTheEndOfLine
                    ? separator + (args.isEmpty() ? "" : String.join(separator, args) + separator)
                    : "";

            final Position toReplace = new Position(offset - userContent.length(), cellLength);
            final Position toSelect = new Position(offset - userContent.length() + 2,
                    newVarProposal.getContent().length() - 3);

            final DocumentModification modification = new DocumentModification(contentSuffix, toReplace, toSelect,
                    () -> {
                        final Collection<IRegion> regionsToLinkedEdit = atTheEndOfLine
                                ? calculateRegionsForLinkedMode(newVarProposal, lineInfo)
                                : new ArrayList<>();
                        return createOperationsToPerformAfterAccepting(regionsToLinkedEdit);
                    });
            proposals.add(new RedCompletionProposalAdapter(assist, newVarProposal, modification));
        }
        return proposals;
    }

    private Collection<IRegion> calculateRegionsForLinkedMode(final RedNewVariableProposal proposal,
            final IRegion lineInformation) {
        final int startingOffset = lineInformation.getOffset();
        final int separatorLength = assist.getSeparatorToFollow().length();
        int offset = startingOffset + proposal.getContent().length() + separatorLength;
        final Region nameRegion = new Region(startingOffset + 2, proposal.getContent().length() - 3);

        final List<IRegion> linkedModeRegions = new ArrayList<>();
        linkedModeRegions.add(nameRegion);
        switch (proposal.getType()) {
            case SCALAR:
            case LIST:
                for (final String arg : proposal.getArguments()) {
                    linkedModeRegions.add(new Region(offset, arg.length()));
                    offset += arg.length() + separatorLength;
                }
                linkedModeRegions.add(new Region(offset, 0));
                break;
            case DICTIONARY:
                for (final String arg : proposal.getArguments()) {
                    final List<String> splittedKeyVal = Splitter.on('=').limit(2).splitToList(arg);
                    linkedModeRegions.add(new Region(offset, splittedKeyVal.get(0).length()));
                    linkedModeRegions.add(
                            new Region(offset + splittedKeyVal.get(0).length() + 1, splittedKeyVal.get(1).length()));

                    offset += arg.length() + separatorLength;
                }
                linkedModeRegions.add(new Region(offset, 0));
                break;
            default:
                throw new IllegalStateException("Unknown variable def value: " + proposal.getType());
        }
        return linkedModeRegions;
    }

    private Collection<Runnable> createOperationsToPerformAfterAccepting(
            final Collection<IRegion> regionsToLinkedEdit) {
        final Collection<Runnable> operations = new ArrayList<>();

        if (!regionsToLinkedEdit.isEmpty()) {
            operations.add(() -> SwtThread
                    .asyncExec(() -> RedEditorLinkedModeUI.enableLinkedMode(viewer, regionsToLinkedEdit)));
        }

        return operations;
    }
}
