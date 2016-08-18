/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.nattable.configs.SuiteModelEditableRule;

/**
 * @author wypych
 */
public class KeywordTableEditableRule extends SuiteModelEditableRule {

    protected KeywordTableEditableRule(boolean isEditable) {
        super(isEditable);
    }

    public static IEditableRule createEditableRule(final RobotSuiteFile fileModel) {
        return new KeywordTableEditableRule(fileModel.isEditable());
    }

    @Override
    public boolean isEditable(final ILayerCell cell, final IConfigRegistry configRegistry) {
        return super.isEditable(cell, configRegistry) && !cell.getConfigLabels().hasLabel(
                KeywordElementsInTreeLabelAccumulator.KEYWORD_DEFINITION_SETTING_DOCUMENTATION_NOT_EDITABLE_LABEL);
    }
}
