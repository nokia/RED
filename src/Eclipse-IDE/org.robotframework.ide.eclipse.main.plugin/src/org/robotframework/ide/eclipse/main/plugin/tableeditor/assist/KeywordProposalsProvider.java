/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.environment.IRuntimeEnvironment;
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

import com.google.common.annotations.VisibleForTesting;

public class KeywordProposalsProvider implements RedContentProposalProvider {

    protected final RobotSuiteFile suiteFile;

    protected final IRowDataProvider<?> dataProvider;

    public KeywordProposalsProvider(final RobotSuiteFile suiteFile, final IRowDataProvider<?> dataProvider) {
        this.suiteFile = suiteFile;
        this.dataProvider = dataProvider;
    }

    @Override
    public boolean shouldShowProposals(final AssistantContext context) {
        final NatTableAssistantContext tableContext = (NatTableAssistantContext) context;
        return !ModelRowUtilities.isLocalSetting(dataProvider, tableContext.getRow())
                && !ModelRowUtilities.getTemplateInUse(dataProvider, tableContext.getRow()).isPresent()
                || ModelRowUtilities.isKeywordBasedLocalSetting(dataProvider, tableContext.getRow());
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {

        final String prefix = contents.substring(0, position);
        final List<? extends AssistProposal> keywordsProposals = new RedKeywordProposals(suiteFile)
                .getKeywordProposals(prefix);

        final Predicate<AssistProposal> shouldCommitAfterAccepting = proposal -> !EmbeddedKeywordNamesSupport
                .hasEmbeddedArguments(proposal.getContent());

        final IRuntimeEnvironment env = suiteFile.getRobotProject().getRuntimeEnvironment();
        return keywordsProposals.stream()
                .map(proposal -> new AssistProposalAdapter(env, proposal, shouldCommitAfterAccepting,
                        () -> createOperationsToPerformAfterAccepting((RedKeywordProposal) proposal,
                                (NatTableAssistantContext) context)))
                .toArray(RedContentProposal[]::new);
    }

    protected List<Runnable> createOperationsToPerformAfterAccepting(final RedKeywordProposal proposedKeyword,
            final NatTableAssistantContext tableContext) {
        final List<Runnable> operations = new ArrayList<>();

        if (!isTemplateSetting(tableContext)) {
            final MultipleCellTableUpdater updater = new MultipleCellTableUpdater(tableContext, dataProvider);
            final List<String> valuesToInsert = getValuesToInsert(proposedKeyword);
            if (shouldInsertMultipleCells(updater, valuesToInsert)) {
                operations.add(() -> updater.insertMultipleCells(valuesToInsert));
            }
        }

        if (!proposedKeyword.isAccessible()) {
            operations.add(() -> new ImportLibraryTableFixer(proposedKeyword.getSourceName()).apply(suiteFile));
        }

        return operations;
    }

    protected boolean isTemplateSetting(final NatTableAssistantContext tableContext) {
        return ModelRowUtilities.isTemplateLocalSetting(dataProvider, tableContext.getRow());
    }

    protected boolean shouldInsertMultipleCells(final MultipleCellTableUpdater updater,
            final List<String> valuesToInsert) {
        return updater.shouldInsertMultipleCells(valuesToInsert);
    }

    @VisibleForTesting
    static List<String> getValuesToInsert(final RedKeywordProposal proposedKeyword) {
        final List<String> values = new ArrayList<>(proposedKeyword.getArguments());
        values.add(0, proposedKeyword.getContent());
        values.remove("");
        return values;
    }

}
