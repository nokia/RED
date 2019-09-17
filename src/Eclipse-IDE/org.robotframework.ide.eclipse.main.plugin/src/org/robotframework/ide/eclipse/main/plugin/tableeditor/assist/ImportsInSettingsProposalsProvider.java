/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class ImportsInSettingsProposalsProvider extends ImportsInCodeProposalsProvider {

    public ImportsInSettingsProposalsProvider(final RobotSuiteFile suiteFile, final IRowDataProvider<?> dataProvider) {
        super(suiteFile, dataProvider);
    }

    @Override
    public boolean shouldShowProposals(final AssistantContext context) {
        final NatTableAssistantContext tableContext = (NatTableAssistantContext) context;
        return tableContext.getColumn() == 1
                && ModelRowUtilities.isKeywordBasedGeneralSetting(dataProvider, tableContext.getRow());
    }

}
