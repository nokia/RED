/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicate;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicates;
import org.robotframework.ide.eclipse.main.plugin.assist.RedWithNameProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposalProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class WithNameElementsProposalsProvider implements RedContentProposalProvider {

    private final IRowDataProvider<?> dataProvider;

    public WithNameElementsProposalsProvider(final IRowDataProvider<?> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {
        final String prefix = contents.substring(0, position);

        final AssistProposalPredicate<String> predicateWordHasToSatisfy = createWordPredicate(
                (NatTableAssistantContext) context);
        final List<? extends AssistProposal> withNameProposals = new RedWithNameProposals(predicateWordHasToSatisfy)
                .getWithNameProposals(prefix);

        return withNameProposals.stream().map(proposal -> new AssistProposalAdapter(proposal, p -> true)).toArray(
                RedContentProposal[]::new);
    }

    private AssistProposalPredicate<String> createWordPredicate(final NatTableAssistantContext context) {
        final int cellIndex = context.getColumn();
        final Object tableElement = dataProvider.getRowObject(context.getRow());
        if (tableElement instanceof RobotSetting
                && RobotSetting.SettingsGroup.LIBRARIES.equals(((RobotSetting) tableElement).getGroup())) {
            return AssistProposalPredicates.withNamePredicate(cellIndex);
        } else {
            return AssistProposalPredicates.alwaysFalse();
        }
    }
}
