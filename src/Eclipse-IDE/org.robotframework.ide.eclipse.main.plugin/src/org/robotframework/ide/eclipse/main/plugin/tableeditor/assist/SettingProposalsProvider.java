/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
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
        final String prefix = contents.substring(0, position);

        final List<IContentProposal> proposals = new ArrayList<>();

        final List<? extends AssistProposal> settingsProposals = new RedSettingProposals(settingTarget)
                .getSettingsProposals(prefix);

        final NatTableAssistantContext tableContext = (NatTableAssistantContext) context;
        if (tableContext.getColumn() == 0) {
            for (final AssistProposal proposedSetting : settingsProposals) {
                proposals.add(new AssistProposalAdapter(proposedSetting));
            }
        }
        return proposals.toArray(new RedContentProposal[0]);
    }
}
