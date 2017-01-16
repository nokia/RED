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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedNewVariableProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedNewVariableProposals;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentationModification;
import org.robotframework.red.jface.text.link.RedEditorLinkedModeUI;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;

/**
 * @author Michal Anglart
 *
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
            final int cellLength, final String prefix, final boolean atTheEndOfLine) throws BadLocationException {

        final String separator = assist.getSeparatorToFollow();
        final List<ICompletionProposal> proposals = newArrayList();

        for (final AssistProposal newVarProposal : new RedNewVariableProposals().getNewVariableProposals()) {
            final List<String> args = newVarProposal.getArguments();
            final String additionalContent = atTheEndOfLine
                    ? separator + (args.isEmpty() ? "" : Joiner.on(separator).join(args) + separator) : "";

            final Position toSelect = new Position(offset - prefix.length() + 2,
                    newVarProposal.getContent().length() - 3);

            final IRegion lineInfo = document.getLineInformationOfOffset(offset);
            final Collection<IRegion> regions = atTheEndOfLine
                    ? getLinkedModeRegions(lineInfo, (RedNewVariableProposal) newVarProposal)
                    : new ArrayList<IRegion>();
            final Collection<Runnable> operations = createOperationsToPerformAfterAccepting(viewer, regions);
            final DocumentationModification modification = new DocumentationModification(additionalContent,
                    new Position(offset - prefix.length(), cellLength), toSelect, operations);
            proposals.add(new RedCompletionProposalAdapter(newVarProposal, modification));
        }
        return proposals;
    }

    private Collection<IRegion> getLinkedModeRegions(final IRegion lineInformation,
            final RedNewVariableProposal proposal) {
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
}
