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
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ImportLibraryTableFixer;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposalProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class KeywordProposalsProvider implements RedContentProposalProvider {

    private final RobotSuiteFile suiteFile;

    private final IRowDataProvider<?> dataProvider;

    public KeywordProposalsProvider(final RobotSuiteFile suiteFile, final IRowDataProvider<?> dataProvider) {
        this.suiteFile = suiteFile;
        this.dataProvider = dataProvider;
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

    private List<Runnable> createOperationsToPerformAfterAccepting(final RedKeywordProposal proposedKeyword,
            final NatTableAssistantContext tableContext) {
        final List<Runnable> operations = new ArrayList<>();

        if (!isTemplateSetting(dataProvider, tableContext.getRow())) {
            final SelectedKeywordTableUpdater updater = new SelectedKeywordTableUpdater(tableContext, dataProvider);
            if (updater.shouldInsertWithArgs(proposedKeyword, values -> true)) {
                operations.add(() -> updater.insertCallWithArgs(proposedKeyword));
            }
        }

        if (!proposedKeyword.isAccessible()) {
            operations.add(() -> new ImportLibraryTableFixer(proposedKeyword.getSourceName()).apply(suiteFile));
        }

        return operations;
    }

    private static boolean isTemplateSetting(final IRowDataProvider<?> dataProvider, final int row) {
        final Object rowObject = dataProvider.getRowObject(row);
        if (rowObject instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) rowObject;
            final ModelType modelType = call.getLinkedElement().getModelType();
            return modelType == ModelType.TEST_CASE_TEMPLATE || modelType == ModelType.TASK_TEMPLATE;
        }
        return false;
    }

}
