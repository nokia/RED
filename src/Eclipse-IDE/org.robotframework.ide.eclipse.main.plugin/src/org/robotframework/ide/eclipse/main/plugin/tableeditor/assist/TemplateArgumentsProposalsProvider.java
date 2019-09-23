/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.assist.RedTemplateArgumentsProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedTemplateArgumentsProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedTextContentAdapter.SubstituteTextModificationStrategy;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class TemplateArgumentsProposalsProvider extends KeywordProposalsProvider {

    public TemplateArgumentsProposalsProvider(final RobotSuiteFile suiteFile, final IRowDataProvider<?> dataProvider) {
        super(suiteFile, dataProvider);
    }

    @Override
    public boolean shouldShowProposals(final AssistantContext context) {
        final NatTableAssistantContext tableContext = (NatTableAssistantContext) context;
        return tableContext.getColumn() == 0 && ModelRowUtilities.isEmptyLine(dataProvider, tableContext.getRow())
                && ModelRowUtilities.getTemplateInUse(dataProvider, tableContext.getRow()).isPresent();
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {
        final NatTableAssistantContext tableContext = (NatTableAssistantContext) context;
        final String templateKeyword = ModelRowUtilities.getTemplateInUse(dataProvider, tableContext.getRow()).get();
        final List<RedTemplateArgumentsProposal> argProposals = new RedTemplateArgumentsProposals(suiteFile)
                .getRedTemplateArgumentsProposal(templateKeyword);

        final IRuntimeEnvironment env = suiteFile.getRobotProject().getRuntimeEnvironment();
        return argProposals.stream()
                .map(proposal -> new AssistProposalAdapter(env, proposal, new TemplateArgumentModificationStrategy(),
                        () -> createOperationsToPerformAfterAccepting(proposal, tableContext)))
                .toArray(RedContentProposal[]::new);
    }

    private static class TemplateArgumentModificationStrategy extends SubstituteTextModificationStrategy {

        @Override
        public boolean shouldSelectAllAfterInsert() {
            return true;
        }

        @Override
        public boolean shouldCommitAfterInsert() {
            return false;
        }
    }
}
