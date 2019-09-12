/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SingleParagraphInput;

class DisableSettingReservedWordProposal extends BaseAssistProposal {

    DisableSettingReservedWordProposal(final ProposalMatch match) {
        super(DisableSettingReservedWordProposals.NONE, match);
    }

    @Override
    public boolean isDocumented() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Disables keyword based setting. It may be used to disable general setting in local setting.";
    }

    @Override
    public DocumentationViewInput getDocumentationInput() {
        return new SingleParagraphInput(this::getDescription);
    }
}
