package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.swt.widgets.Event;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RedNatTableContentTooltip;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SuiteFileMarkersContainer;

class CasesTableContentTooltip extends RedNatTableContentTooltip {

    private final Map<String, String> tooltips = new HashMap<>();

    public CasesTableContentTooltip(final NatTable natTable, final SuiteFileMarkersContainer markersContainer,
            final IRowDataProvider<?> dataProvider) {
        super(natTable, markersContainer, dataProvider);

        tooltips.put("[" + RobotCase.TAGS.toLowerCase() + "]",
                "These tags are set to this test case and they possibly override Default Tags");
        tooltips.put("[" + RobotCase.SETUP.toLowerCase() + "]",
                "The keyword $s is executed before other keywords inside the definition");
        tooltips.put("[" + RobotCase.TEMPLATE.toLowerCase() + "]", "The keyword %s is used as a template");
        tooltips.put("[" + RobotCase.TIMEOUT.toLowerCase() + "]",
                "Specifies maximum time this test case is allowed to execute before being aborted.\n"
                        + "This setting overrides Test Timeout setting set on suite level\n"
                        + "Numerical values are intepreted as seconds but special syntax like '1min 15s' or '2 hours' can be used.");
        tooltips.put("[" + RobotCase.TEARDOWN.toLowerCase() + "]",
                "The keyword %s is executed after every other keyword inside the definition");
    }


    @Override
    protected String getText(final Event event) {
        String text = super.getText(event);

        final int col = this.natTable.getColumnPositionByX(event.x);
        if (col == 1 && text != null && tooltips.containsKey(text.toLowerCase())) {
            final int row = this.natTable.getRowPositionByY(event.y);
            final ILayerCell cell = this.natTable.getCellByPosition(col + 1, row);
            final String keyword = cell != null && cell.getDataValue() != null
                    && !((String) cell.getDataValue()).isEmpty() ? (String) cell.getDataValue()
                            : "given in first argument";
            text = String.format(tooltips.get(text.toLowerCase()), keyword);
        }
        return text;
    }
}