package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.swt.widgets.Event;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RedNatTableContentTooltip;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SuiteFileMarkersContainer;

class KeywordsTableContentTooltip extends RedNatTableContentTooltip {

    private static final String TAGS = "[tags]";
    private static final String TEARDOWN = "[teardown]";
    private static final String TIMEOUT = "[timeout]";
    private static final String RETURN = "[return]";

    private final Map<String, String> tooltips = new HashMap<>();

    public KeywordsTableContentTooltip(final NatTable natTable, final SuiteFileMarkersContainer markersContainer,
            final IRowDataProvider<?> dataProvider) {
        super(natTable, markersContainer, dataProvider);
        tooltips.put(TAGS,
                "These tags are set to this keyword and are not affected by Default Tags or Force Tags setting");
        tooltips.put(TEARDOWN, "The keyword %s is executed after every other keyword inside the definition");
        tooltips.put(TIMEOUT, "Specifies maximum time this keyword is allowed to execute before being aborted.\n"
                + "This setting overrides Test Timeout setting set on suite level\n"
                + "Numerical values are intepreted as seconds but special syntax like '1min 15s' or '2 hours' can be used.");
        tooltips.put(RETURN, "Specify the return value for this keyword. Multiple values can be used.");
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