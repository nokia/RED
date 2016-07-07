package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;


public class GeneralSettingsAssistanceLabelAccumulator implements IConfigLabelAccumulator {

    static final String KEYWORD_ASSIST_REQUIRED = "KEYWORD_ASSIST_REQUIRED";

    static final String VARIABLES_ASSIST_REQUIRED = "VARIABLES_ASSIST_REQUIRED";

    private final GeneralSettingsDataProvider dataProvider;

    public GeneralSettingsAssistanceLabelAccumulator(final GeneralSettingsDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        final Entry<String, RobotElement> entry = dataProvider.getRowObject(rowPosition);

        final boolean isKeywordBased = GeneralSettingsModel.isKeywordBased(entry);
        final boolean isTemplate = GeneralSettingsModel.isTemplate(entry);
        
        if ((isKeywordBased || isTemplate)  && columnPosition == 1) {
            configLabels.addLabel(KEYWORD_ASSIST_REQUIRED);

        } else if (isKeywordBased && columnPosition > 1
                && columnPosition < dataProvider.getColumnCount() - 1) {
            configLabels.addLabel(VARIABLES_ASSIST_REQUIRED);
        }
    }
}
