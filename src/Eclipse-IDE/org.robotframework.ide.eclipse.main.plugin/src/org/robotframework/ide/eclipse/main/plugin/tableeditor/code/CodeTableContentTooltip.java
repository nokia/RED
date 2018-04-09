/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.swt.widgets.Event;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals.SettingTarget;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RedNatTableContentTooltip;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SuiteFileMarkersContainer;

public class CodeTableContentTooltip extends RedNatTableContentTooltip {

    private final SettingTarget settingTarget;

    public CodeTableContentTooltip(final NatTable natTable, final SuiteFileMarkersContainer markersContainer,
            final IRowDataProvider<?> dataProvider, final SettingTarget settingTarget) {
        super(natTable, markersContainer, dataProvider);
        this.settingTarget = settingTarget;
    }

    @Override
    protected String getText(final Event event) {
        final String text = super.getText(event);

        final int col = natTable.getColumnPositionByX(event.x);
        if (col == 1 && RedSettingProposals.isSetting(settingTarget, text)) {
            final int row = natTable.getRowPositionByY(event.y);
            final ILayerCell cell = natTable.getCellByPosition(col + 1, row);
            final String keyword = cell != null && cell.getDataValue() != null
                    && !((String) cell.getDataValue()).isEmpty() ? (String) cell.getDataValue() : "";
            return RedSettingProposals.getSettingDescription(settingTarget, text, keyword);
        }
        return text;
    }
}