/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals.SettingTarget;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.CodeReservedWordsProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.CombinedProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.ImportsInCodeProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.KeywordProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.SettingProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.VariableProposalsProvider;
import org.robotframework.red.nattable.edit.RedTextCellEditor;

public class CodeReservedWordsTableEditConfiguration extends AbstractRegistryConfiguration {

    private final RobotSuiteFile suiteFile;

    private final IRowDataProvider<?> dataProvider;

    private final SettingTarget settingsProposalsTarget;

    private final boolean wrapCellContent;

    public CodeReservedWordsTableEditConfiguration(final RobotSuiteFile suiteFile, final IRowDataProvider<?> dataProvider,
            final SettingTarget settingsProposalsTarget,
            final boolean wrapCellContent) {
        this.suiteFile = suiteFile;
        this.dataProvider = dataProvider;
        this.settingsProposalsTarget = settingsProposalsTarget;
        this.wrapCellContent = wrapCellContent;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final IRuntimeEnvironment env = suiteFile.getRuntimeEnvironment();
        final CombinedProposalsProvider proposalProvider = new CombinedProposalsProvider(
                new SettingProposalsProvider(env, settingsProposalsTarget),
                new CodeReservedWordsProposalsProvider(env, dataProvider),
                new KeywordProposalsProvider(suiteFile, dataProvider),
                new ImportsInCodeProposalsProvider(suiteFile, dataProvider),
                new VariableProposalsProvider(suiteFile, dataProvider));

        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new RedTextCellEditor(proposalProvider, wrapCellContent, dataProvider), DisplayMode.NORMAL,
                TableConfigurationLabels.ASSIST_REQUIRED);
    }
}
