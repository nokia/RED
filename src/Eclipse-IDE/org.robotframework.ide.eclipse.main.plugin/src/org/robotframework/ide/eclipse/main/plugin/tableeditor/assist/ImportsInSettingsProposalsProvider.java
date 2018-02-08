/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
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
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {
        if (!areApplicable((NatTableAssistantContext) context)) {
            return new RedContentProposal[0];
        }

        final String prefix = contents.substring(0, position);
        final List<? extends AssistProposal> importProposals = new RedImportProposals(suiteFile)
                .getImportsProposals(prefix);

        return importProposals.stream().map(AssistProposalAdapter::new).toArray(RedContentProposal[]::new);
    }

    private boolean areApplicable(final NatTableAssistantContext tableContext) {
        return tableContext.getColumn() == 1
                && KeywordProposalsInSettingsProvider.isKeywordBasedSetting(dataProvider, tableContext.getRow());
    }

}
