package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals.SettingTarget;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.CodeReservedElementsProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.CombinedProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.ImportsInCodeProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.KeywordProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.SettingProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.VariableProposalsProvider;
import org.robotframework.red.nattable.edit.RedTextCellEditor;

public class KeywordsTableEditConfiguration extends AbstractRegistryConfiguration {

    private final RobotSuiteFile suiteFile;

    private final IRowDataProvider<?> dataProvider;

    private final boolean wrapCellContent;

    public KeywordsTableEditConfiguration(final RobotSuiteFile suiteFile, final IRowDataProvider<?> dataProvider,
            final boolean wrapCellContent) {
        this.suiteFile = suiteFile;
        this.dataProvider = dataProvider;
        this.wrapCellContent = wrapCellContent;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final CombinedProposalsProvider proposalProvider = new CombinedProposalsProvider(
                new SettingProposalsProvider(SettingTarget.KEYWORD),
                new CodeReservedElementsProposalsProvider(dataProvider),
                new KeywordProposalsProvider(suiteFile, dataProvider),
                new ImportsInCodeProposalsProvider(suiteFile),
                new VariableProposalsProvider(suiteFile, dataProvider));

        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new RedTextCellEditor(proposalProvider, wrapCellContent), DisplayMode.NORMAL,
                TableConfigurationLabels.ASSIST_REQUIRED);
    }
}
