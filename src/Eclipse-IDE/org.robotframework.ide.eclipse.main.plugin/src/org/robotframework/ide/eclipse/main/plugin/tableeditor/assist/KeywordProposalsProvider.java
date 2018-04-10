/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ImportLibraryTableFixer;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposalProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class KeywordProposalsProvider implements RedContentProposalProvider {

    private final Supplier<RobotSuiteFile> suiteFile;

    private final Optional<IRowDataProvider<?>> dataProvider;

    public KeywordProposalsProvider(final RobotSuiteFile suiteFile, final IRowDataProvider<?> dataProvider) {
        this(() -> suiteFile, dataProvider);
    }

    public KeywordProposalsProvider(final Supplier<RobotSuiteFile> suiteFile, final IRowDataProvider<?> dataProvider) {
        this.suiteFile = suiteFile;
        this.dataProvider = Optional.ofNullable(dataProvider);
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {
        final String prefix = contents.substring(0, position);
        final List<? extends AssistProposal> keywordsProposals = new RedKeywordProposals(suiteFile.get())
                .getKeywordProposals(prefix);

        final RobotRuntimeEnvironment env = suiteFile.get().getProject().getRuntimeEnvironment();
        if (!dataProvider.isPresent()) {
            return keywordsProposals.stream().map(proposal -> new AssistProposalAdapter(env, proposal)).toArray(
                    RedContentProposal[]::new);
        }

        final Predicate<AssistProposal> shouldCommitAfterAccepting = proposal -> !EmbeddedKeywordNamesSupport
                .hasEmbeddedArguments(proposal.getContent());

        return keywordsProposals.stream()
                .map(proposal -> new AssistProposalAdapter(env, proposal, shouldCommitAfterAccepting,
                        () -> createOperationsToPerformAfterAccepting((RedKeywordProposal) proposal,
                                (NatTableAssistantContext) context)))
                .toArray(RedContentProposal[]::new);
    }

    private List<Runnable> createOperationsToPerformAfterAccepting(final RedKeywordProposal proposedKeyword,
            final NatTableAssistantContext tableContext) {
        final List<Runnable> operations = new ArrayList<>();

        final SelectedKeywordTableUpdater updater = new SelectedKeywordTableUpdater(tableContext, dataProvider.get());
        if (updater.shouldInsertWithArgs(proposedKeyword, values -> true)) {
            operations.add(() -> updater.insertCallWithArgs(proposedKeyword));
        }

        if (!proposedKeyword.isAccessible()) {
            operations.add(() -> new ImportLibraryTableFixer(proposedKeyword.getSourceName()).apply(suiteFile.get()));
        }

        return operations;
    }
}
