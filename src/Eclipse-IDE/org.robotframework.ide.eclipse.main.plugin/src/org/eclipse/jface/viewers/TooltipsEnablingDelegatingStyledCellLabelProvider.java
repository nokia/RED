package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * This class enables possibility to provide and customize tooltips when using
 * styled label providers.
 * 
 * @author Michal Anglart
 * 
 */
public class TooltipsEnablingDelegatingStyledCellLabelProvider extends DelegatingStyledCellLabelProvider implements
        ILabelProvider {

    private final IStyledLabelProvider labelProvider;

    public TooltipsEnablingDelegatingStyledCellLabelProvider(final IStyledLabelProvider labelProvider) {
        super(labelProvider);
        this.labelProvider = labelProvider;
    }

    @Override
    public Color getToolTipBackgroundColor(final Object object) {
        if (labelProvider instanceof CellLabelProvider) {
            return ((CellLabelProvider) labelProvider).getToolTipBackgroundColor(object);
        }
        return super.getToolTipBackgroundColor(object);
    }

    @Override
    public int getToolTipDisplayDelayTime(final Object object) {
        if (labelProvider instanceof CellLabelProvider) {
            return ((CellLabelProvider) labelProvider).getToolTipDisplayDelayTime(object);
        }
        return super.getToolTipDisplayDelayTime(object);
    }

    @Override
    public Font getToolTipFont(final Object object) {
        if (labelProvider instanceof CellLabelProvider) {
            return ((CellLabelProvider) labelProvider).getToolTipFont(object);
        }
        return super.getToolTipFont(object);
    }

    @Override
    public Color getToolTipForegroundColor(final Object object) {
        if (labelProvider instanceof CellLabelProvider) {
            return ((CellLabelProvider) labelProvider).getToolTipForegroundColor(object);
        }
        return super.getToolTipForegroundColor(object);
    }

    @Override
    public Image getToolTipImage(final Object object) {
        if (labelProvider instanceof CellLabelProvider) {
            return ((CellLabelProvider) labelProvider).getToolTipImage(object);
        }
        return super.getToolTipImage(object);
    }

    @Override
    public Point getToolTipShift(final Object object) {
        if (labelProvider instanceof CellLabelProvider) {
            return ((CellLabelProvider) labelProvider).getToolTipShift(object);
        }
        return super.getToolTipShift(object);
    }

    @Override
    public int getToolTipStyle(final Object object) {
        if (labelProvider instanceof CellLabelProvider) {
            return ((CellLabelProvider) labelProvider).getToolTipStyle(object);
        }
        return super.getToolTipStyle(object);
    }

    @Override
    public String getToolTipText(final Object element) {
        if (labelProvider instanceof CellLabelProvider) {
            return ((CellLabelProvider) labelProvider).getToolTipText(element);
        }
        return super.getToolTipText(element);
    }

    @Override
    public int getToolTipTimeDisplayed(final Object object) {
        if (labelProvider instanceof CellLabelProvider) {
            return ((CellLabelProvider) labelProvider).getToolTipTimeDisplayed(object);
        }
        return super.getToolTipTimeDisplayed(object);
    }

    @Override
    public boolean useNativeToolTip(final Object object) {
        if (labelProvider instanceof CellLabelProvider) {
            return ((CellLabelProvider) labelProvider).useNativeToolTip(object);
        }
        return super.useNativeToolTip(object);
    }

    @Override
    public String getText(final Object element) {
        return "";
    }
}
