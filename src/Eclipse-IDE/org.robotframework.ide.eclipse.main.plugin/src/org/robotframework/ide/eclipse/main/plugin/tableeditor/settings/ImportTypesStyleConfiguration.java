package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.AbstractCellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.painter.RedTableTextPainter;


class ImportTypesStyleConfiguration extends AbstractRegistryConfiguration {

    private final TableTheme theme;

    private final boolean wrapCellContent;

    public ImportTypesStyleConfiguration(final TableTheme theme, final boolean wrapCellContent) {
        this.theme = theme;
        this.wrapCellContent = wrapCellContent;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final ICellPainter caseCellPainter = new CellPainterDecorator(new RedTableTextPainter(wrapCellContent, 2),
                CellEdgeEnum.RIGHT, new DropDownArrowPainter(theme.getBodyForeground()));

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, caseCellPainter, DisplayMode.NORMAL,
                ImportTypesLabelAccumulator.IMPORT_TYPE_LABEL);
    }

    private static class DropDownArrowPainter extends AbstractCellPainter {

        private final Color arrowColor;

        public DropDownArrowPainter(final Color arrowColor) {
            this.arrowColor = arrowColor;
        }

        @Override
        public int getPreferredWidth(final ILayerCell cell, final GC gc, final IConfigRegistry configRegistry) {
            return 10;
        }

        @Override
        public int getPreferredHeight(final ILayerCell cell, final GC gc, final IConfigRegistry configRegistry) {
            return 3;
        }

        @Override
        public void paintCell(final ILayerCell cell, final GC gc, final Rectangle bounds,
                final IConfigRegistry configRegistry) {

            final Color originalBg = gc.getBackground();

            gc.setBackground(arrowColor);
            gc.fillPolygon(new int[] { bounds.x, bounds.y, bounds.x + 2, bounds.y + 2, bounds.x + 4, bounds.y });
            gc.setBackground(originalBg);
        }
    }
}
