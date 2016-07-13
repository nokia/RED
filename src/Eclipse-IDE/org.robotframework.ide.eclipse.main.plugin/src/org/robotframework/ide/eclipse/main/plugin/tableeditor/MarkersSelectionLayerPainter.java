package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayerPainter;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;

public class MarkersSelectionLayerPainter extends SelectionLayerPainter {

    public MarkersSelectionLayerPainter() {
        super(ColorsManager.getColor(250, 250, 250));
    }

    @Override
    public void paintLayer(
            final ILayer natLayer, final GC gc,
            final int xOffset, final int yOffset, final Rectangle pixelRectangle,
            final IConfigRegistry configRegistry) {
        super.paintLayer(natLayer, gc, xOffset, yOffset, pixelRectangle, configRegistry);

        final Rectangle positionRectangle = getPositionRectangleFromPixelRectangle(natLayer, pixelRectangle);
        final int columnPositionOffset = positionRectangle.x;
        final int rowPositionOffset = positionRectangle.y;

        final int numberOfRows = rowPositionOffset + positionRectangle.height;
        final int noOfColumns = columnPositionOffset + positionRectangle.width;

        for (int row = rowPositionOffset; row < numberOfRows; row++) {
            for (int column = columnPositionOffset; column < noOfColumns; column++) {

                final ILayerCell currentCell = natLayer.getCellByPosition(column, row);
                if (currentCell != null) {
                    final Rectangle currentCellBounds = currentCell.getBounds();

                    Image image = null;
                    final LabelStack labels = natLayer.getConfigLabelsByPosition(column, row);
                    if (labels.hasLabel(MarkersLabelAccumulator.ERROR_MARKER_LABEL)) {
                        image = ImagesManager.getImage(RedImages.getErrorImage());
                    } else if (labels.hasLabel(MarkersLabelAccumulator.WARNING_MARKER_LABEL)) {
                        image = ImagesManager.getImage(RedImages.getWarningImage());
                    }

                    if (image != null) {
                        final Rectangle imageBounds = image.getBounds();
                        gc.drawImage(image, currentCellBounds.x + (currentCellBounds.width - imageBounds.width) / 2 - 2,
                                currentCellBounds.y + (currentCellBounds.height - imageBounds.height) / 2 - 1);
                    }
                }
            }
        }
    }
}
