/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesElementsLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.KeywordsElementsLabelAccumulator;

/**
 * @author lwlodarc
 *
 */
public class VariablesInElementsLabelAccumulator implements IConfigLabelAccumulator {

    public static final String POSSIBLE_VARIABLES_IN_ELEMENTS_CONFIG_LABEL = "POSSIBLE_VARIABLES_IN_ELEMENTS_INSIDE";

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        if (!(configLabels.hasLabel(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL)
                || configLabels.hasLabel(KeywordsElementsLabelAccumulator.KEYWORD_DEFINITION_CONFIG_LABEL)
                || configLabels.hasLabel(CasesElementsLabelAccumulator.CASE_CONFIG_LABEL))) {
            configLabels.addLabel(POSSIBLE_VARIABLES_IN_ELEMENTS_CONFIG_LABEL);
        }
    }
}