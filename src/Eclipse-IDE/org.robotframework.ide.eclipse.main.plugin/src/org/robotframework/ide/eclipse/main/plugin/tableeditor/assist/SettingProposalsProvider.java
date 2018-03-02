/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals.SettingTarget;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposalProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class SettingProposalsProvider implements RedContentProposalProvider {

    private final SettingTarget settingTarget;

    public SettingProposalsProvider(final SettingTarget settingTarget) {
        this.settingTarget = settingTarget;
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {
        if (!areApplicable((NatTableAssistantContext) context)) {
            return new RedContentProposal[0];
        }

        final String prefix = contents.substring(0, position);
        final List<? extends AssistProposal> settingsProposals = new RedSettingProposals(settingTarget)
                .getSettingsProposals(prefix);

        return settingsProposals.stream().map(proposal -> new AssistProposalAdapter(proposal, p -> true)).toArray(
                RedContentProposal[]::new);
    }

    private boolean areApplicable(final NatTableAssistantContext tableContext) {
        return tableContext.getColumn() == 0;
    }
}
