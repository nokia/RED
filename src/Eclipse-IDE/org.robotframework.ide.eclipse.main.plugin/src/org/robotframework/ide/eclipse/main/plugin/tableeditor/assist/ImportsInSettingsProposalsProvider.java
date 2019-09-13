/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedImportProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposalProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class ImportsInSettingsProposalsProvider implements RedContentProposalProvider {

    private final RobotSuiteFile suiteFile;

    private final IRowDataProvider<?> dataProvider;

    public ImportsInSettingsProposalsProvider(final RobotSuiteFile suiteFile, final IRowDataProvider<?> dataProvider) {
        this.suiteFile = suiteFile;
        this.dataProvider = dataProvider;
    }

    @Override
    public boolean shouldShowProposals(final AssistantContext context) {
        final NatTableAssistantContext tableContext = (NatTableAssistantContext) context;
        return tableContext.getColumn() == 1
                && ModelRowUtilities.isKeywordBasedGeneralSetting(dataProvider, tableContext.getRow());
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {

        final String prefix = contents.substring(0, position);
        final List<? extends AssistProposal> importProposals = new RedImportProposals(suiteFile)
                .getImportsProposals(prefix);

        final IRuntimeEnvironment env = suiteFile.getRobotProject().getRuntimeEnvironment();
        return importProposals.stream().map(proposal -> new AssistProposalAdapter(env, proposal)).toArray(
                RedContentProposal[]::new);
    }

}
