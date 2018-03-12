/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.CombinedProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.ImportsProposalsProvider.LibrariesProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.ImportsProposalsProvider.ResourceFileLocationsProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.ImportsProposalsProvider.VariableFileLocationsProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.VariableProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.WithNameElementsProposalsProvider;
import org.robotframework.red.nattable.edit.RedComboBoxCellEditor;
import org.robotframework.red.nattable.edit.RedTextCellEditor;


class ImportsSettingsEditConfiguration extends AbstractRegistryConfiguration {

    private final RobotSuiteFile suiteFile;

    private final IRowDataProvider<?> dataProvider;

    private final boolean wrapCellContent;

    ImportsSettingsEditConfiguration(final RobotSuiteFile suiteFile,
            final IRowDataProvider<?> dataProvider, final boolean wrapCellContent) {
        this.suiteFile = suiteFile;
        this.dataProvider = dataProvider;
        this.wrapCellContent = wrapCellContent;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final CombinedProposalsProvider proposalProvider = new CombinedProposalsProvider(
                new VariableFileLocationsProposalsProvider(suiteFile, dataProvider),
                new ResourceFileLocationsProposalsProvider(suiteFile, dataProvider),
                new WithNameElementsProposalsProvider(dataProvider),
                new LibrariesProposalsProvider(suiteFile, dataProvider),
                new VariableProposalsProvider(suiteFile, dataProvider));

        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new RedTextCellEditor(proposalProvider, wrapCellContent), DisplayMode.NORMAL,
                TableConfigurationLabels.ASSIST_REQUIRED);

        final List<String> possibleImports = newArrayList(SettingsGroup.LIBRARIES.getName(),
                SettingsGroup.RESOURCES.getName(), SettingsGroup.VARIABLES.getName());
        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new RedComboBoxCellEditor(possibleImports), DisplayMode.NORMAL,
                ImportTypesLabelAccumulator.IMPORT_TYPE_LABEL);
    }
}
