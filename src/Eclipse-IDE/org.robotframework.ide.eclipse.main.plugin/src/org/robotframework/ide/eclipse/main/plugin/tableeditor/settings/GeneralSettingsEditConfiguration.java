package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.robotframework.ide.eclipse.main.plugin.assist.KeywordsContentProposingSupport;
import org.robotframework.ide.eclipse.main.plugin.assist.VariablesContentProposingSupport;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.nattable.edit.RedTextCellEditor;


public class GeneralSettingsEditConfiguration extends AbstractRegistryConfiguration {

    private final RobotSuiteFile suiteFile;

    public GeneralSettingsEditConfiguration(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final VariablesContentProposingSupport varProposalsSupport = new VariablesContentProposingSupport(suiteFile);
        final KeywordsContentProposingSupport kwProposalSupport = new KeywordsContentProposingSupport(suiteFile);

        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new RedTextCellEditor(varProposalsSupport), DisplayMode.NORMAL,
                GeneralSettingsAssistanceLabelAccumulator.VARIABLES_ASSIST_REQUIRED);

        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR,
                new RedTextCellEditor(kwProposalSupport), DisplayMode.NORMAL,
                GeneralSettingsAssistanceLabelAccumulator.KEYWORD_ASSIST_REQUIRED);
    }
}
