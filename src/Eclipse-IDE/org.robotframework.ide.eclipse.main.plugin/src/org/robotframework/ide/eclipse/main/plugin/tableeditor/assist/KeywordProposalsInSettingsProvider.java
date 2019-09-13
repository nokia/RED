/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class KeywordProposalsInSettingsProvider extends KeywordProposalsProvider {

    public KeywordProposalsInSettingsProvider(final RobotSuiteFile suiteFile, final IRowDataProvider<?> dataProvider) {
        super(suiteFile, dataProvider);
    }

    @Override
    public boolean shouldShowProposals(final AssistantContext context) {
        final NatTableAssistantContext tableContext = (NatTableAssistantContext) context;
        return tableContext.getColumn() == 1
                && ModelRowUtilities.isKeywordBasedGeneralSetting(dataProvider, tableContext.getRow());
    }

    @Override
    protected boolean isTemplateSetting(final NatTableAssistantContext tableContext) {
        return ModelRowUtilities.isTemplateGeneralSetting(dataProvider, tableContext.getRow());
    }

    @Override
    protected boolean shouldInsertMultipleCells(final MultipleCellTableUpdater updater,
            final List<String> valuesToInsert) {
        return updater.shouldInsertMultipleCellsWithoutColumnExceeding(valuesToInsert);
    }

}
