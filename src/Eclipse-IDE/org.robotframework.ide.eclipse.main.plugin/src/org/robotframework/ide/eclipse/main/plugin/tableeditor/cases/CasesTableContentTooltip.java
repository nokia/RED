package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.swt.widgets.Event;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RedNatTableContentTooltip;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SuiteFileMarkersContainer;

class CasesTableContentTooltip extends RedNatTableContentTooltip {

    private static final String SETUP = "[setup]";
    private static final String TEARDOWN = "[teardown]";
    private static final String TIMEOUT = "[timeout]";
    private static final String TEMPLATE = "[template]";
    private static final String TAGS = "[tags]";

    private final Map<String, String> tooltips = new HashMap<>();

    public CasesTableContentTooltip(final NatTable natTable, final SuiteFileMarkersContainer markersContainer,
            final IRowDataProvider<?> dataProvider) {
        super(natTable, markersContainer, dataProvider);

        tooltips.put(TAGS, "These tags are set to this test case and they possibly override Default Tags");
        tooltips.put(SETUP, "The keyword $s is executed before other keywords inside the definition");
        tooltips.put(TEMPLATE, "The keyword %s is used as a template");
        tooltips.put(TIMEOUT, "Specifies maximum time this test case is allowed to execute before being aborted.\n"
                + "This setting overrides Test Timeout setting set on suite level\n"
                + "Numerical values are intepreted as seconds but special syntax like '1min 15s' or '2 hours' can be used.");
        tooltips.put(TEARDOWN, "The keyword %s is executed after every other keyword inside the definition");
    }


    @Override
    protected String getText(final Event event) {
        String text = super.getText(event);

        final int col = natTable.getColumnPositionByX(event.x);
        if (col == 1 && text != null && tooltips.containsKey(text.toLowerCase())) {
            final int row = natTable.getRowPositionByY(event.y);
            final ILayerCell cell = natTable.getCellByPosition(col + 1, row);
            final String keyword = cell != null && cell.getDataValue() != null
                    && !((String) cell.getDataValue()).isEmpty() ? (String) cell.getDataValue()
                            : "given in first argument";
            text = String.format(tooltips.get(text.toLowerCase()), keyword);
        }
        return text;
    }
}