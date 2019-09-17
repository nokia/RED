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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.LinkedModeStrategy;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ImportLibraryFixer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentModification;
import org.robotframework.red.jface.text.link.RedEditorLinkedModeUI;
import org.robotframework.red.swt.SwtThread;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 */
public class KeywordCallsAssistProcessor extends RedContentAssistProcessor {

    public KeywordCallsAssistProcessor(final SuiteSourceAssistantContext assist) {
        super(assist);
    }

    @Override
    public String getProposalsTitle() {
        return "Keywords";
    }

    @Override
    public List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.TEST_CASES_SECTION, SuiteSourcePartitionScanner.TASKS_SECTION,
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        return isInApplicableContentType(document, offset)
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) > 0
                && (!ModelUtilities.isLocalSetting(assist.getModel(), offset)
                        && !ModelUtilities.getTemplateInUse(assist.getModel(), offset).isPresent()
                        || ModelUtilities.isKeywordBasedLocalSetting(assist.getModel(), offset));
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final IDocument document, final int offset,
            final int cellLength, final String userContent, final boolean atTheEndOfLine) throws BadLocationException {

        final List<RedKeywordProposal> kwProposals = new RedKeywordProposals(assist.getModel())
                .getKeywordProposals(userContent);

        final String separator = assist.getSeparatorToFollow();
        final List<ICompletionProposal> proposals = new ArrayList<>();

        final boolean shouldInsertArguments = atTheEndOfLine && !isTemplateSetting(offset);
        for (final RedKeywordProposal kwProposal : kwProposals) {
            final List<String> args = shouldInsertArguments ? kwProposal.getArguments() : new ArrayList<>();
            final String contentSuffix = args.isEmpty() ? "" : (separator + String.join(separator, args));

            final Position toReplace = new Position(offset - userContent.length(), cellLength);

            final DocumentModification modification = new DocumentModification(contentSuffix, toReplace, () -> {
                final Collection<IRegion> regionsToLinkedEdit = shouldInsertArguments
                        ? calculateRegionsForLinkedMode(kwProposal, toReplace.getOffset())
                        : new ArrayList<>();
                return createOperationsToPerformAfterAccepting(regionsToLinkedEdit, kwProposal);
            });
            final IContextInformation contextInfo = new ContextInformation(null,
                    kwProposal.getArgumentsDescriptor().getDescription());
            proposals.add(new RedCompletionProposalAdapter(assist, kwProposal, modification, contextInfo));
        }
        return proposals;
    }

    protected boolean isTemplateSetting(final int offset) {
        return ModelUtilities.isTemplateLocalSetting(assist.getModel(), offset);
    }

    @VisibleForTesting
    Collection<IRegion> calculateRegionsForLinkedMode(final RedKeywordProposal proposal, final int startOffset) {
        if (EmbeddedKeywordNamesSupport.hasEmbeddedArguments(proposal.getNameFromDefinition())) {
            return calculateRegionsForLinkedModeOfEmbeddedKeyword(startOffset, proposal.getContent());

        } else {
            final int separatorLength = assist.getSeparatorToFollow().length();
            return calculateRegionsForLinkedModeOfRegularKeyword(startOffset, proposal.getContent(),
                    proposal.getArguments(), separatorLength);
        }
    }

    private static Collection<IRegion> calculateRegionsForLinkedModeOfEmbeddedKeyword(final int startOffset,
            final String wholeContent) {
        final Collection<IRegion> regions = new ArrayList<>();
        final Matcher matcher = Pattern.compile("\\$\\{[^\\}]+\\}").matcher(wholeContent);
        while (matcher.find()) {
            regions.add(new Region(startOffset + matcher.start(), matcher.end() - matcher.start()));
        }
        return regions;
    }

    private static Collection<IRegion> calculateRegionsForLinkedModeOfRegularKeyword(final int startOffset,
            final String wholeContent, final List<String> arguments, final int separatorLength) {
        final Collection<IRegion> regions = new ArrayList<>();
        int offset = startOffset + wholeContent.length();
        if (!arguments.isEmpty()) {
            offset += separatorLength;
        }
        for (final String requiredArg : arguments) {
            regions.add(new Region(offset, requiredArg.length()));
            offset += requiredArg.length() + separatorLength;
        }
        return regions;
    }

    private Collection<Runnable> createOperationsToPerformAfterAccepting(final Collection<IRegion> regionsToLinkedEdit,
            final RedKeywordProposal proposal) {
        final Collection<Runnable> operations = new ArrayList<>();
        if (!regionsToLinkedEdit.isEmpty()) {
            final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
            final LinkedModeStrategy mode = preferences.getAssistantLinkedArgumentsMode();
            final String separator = assist.getSeparatorToFollow();

            final ArgumentsDescriptor argsDescriptor = proposal.getArgumentsDescriptor();
            final boolean hasUpperBound = !argsDescriptor.getVarargArgument().isPresent()
                    && !argsDescriptor.getKwargArgument().isPresent();
            final int numberOfDefaultArgs = argsDescriptor.getDefaultArguments().size();
            operations.add(
                    () -> SwtThread.asyncExec(() -> RedEditorLinkedModeUI.enableLinkedModeWithEmptyCellReplacing(viewer,
                            mode, separator, regionsToLinkedEdit, numberOfDefaultArgs, hasUpperBound)));
        }

        if (!proposal.isAccessible()) {
            operations.add(() -> SwtThread.asyncExec(() -> {
                final ImportLibraryFixer fixer = new ImportLibraryFixer(proposal.getSourceName());
                final Optional<ICompletionProposal> completionProposal = fixer.asContentProposal(null,
                        viewer.getDocument(), assist.getModel());
                if (completionProposal.isPresent()) {
                    completionProposal.get().apply(viewer.getDocument());
                }
            }));
        }

        return operations;
    }
}
