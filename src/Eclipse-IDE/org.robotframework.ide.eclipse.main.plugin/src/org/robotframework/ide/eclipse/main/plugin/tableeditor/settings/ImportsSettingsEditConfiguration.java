package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.CombinedProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.ImportsProposalsProvider.LibrariesProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.ImportsProposalsProvider.ResourceFileLocationsProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.ImportsProposalsProvider.VariableFileLocationsProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.VariableProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.WithNameElementsProposalsProvider;
import org.robotframework.red.nattable.edit.RedTextCellEditor;


class ImportsSettingsEditConfiguration extends AbstractRegistryConfiguration {

    private final RobotSuiteFile suiteFile;

    private final IRowDataProvider<?> dataProvider;

    ImportsSettingsEditConfiguration(final RobotSuiteFile suiteFile,
            final IRowDataProvider<?> dataProvider) {
        this.suiteFile = suiteFile;
        this.dataProvider = dataProvider;
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
                new RedTextCellEditor(proposalProvider), DisplayMode.NORMAL, TableConfigurationLabels.ASSIST_REQUIRED);
    }
}
