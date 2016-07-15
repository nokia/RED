/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.nattable.AddingElementLabelAccumulator;

/**
 * @author Michal Anglart
 */
public class AddingElementStyleConfiguration extends AbstractRegistryConfiguration {

    private final Font font;

    private final boolean isEditable;

    public AddingElementStyleConfiguration(final TableTheme theme, final boolean isEditable) {
        this.font = theme.getFont();
        this.isEditable = isEditable;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final Style style = new Style();

        final Color foregroundColor = isEditable ? ColorsManager.getColor(30, 127, 60)
                : ColorsManager.getColor(200, 200, 200);

        style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, foregroundColor);
        style.setAttributeValue(CellStyleAttributes.FONT, getFont(font, SWT.ITALIC));

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL,
                AddingElementLabelAccumulator.ELEMENT_ADDER_CONFIG_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT,
                AddingElementLabelAccumulator.ELEMENT_ADDER_CONFIG_LABEL);

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL,
                AddingElementLabelAccumulator.ELEMENT_MULTISTATE_ADDER_CONFIG_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT,
                AddingElementLabelAccumulator.ELEMENT_MULTISTATE_ADDER_CONFIG_LABEL);

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL,
                AddingElementLabelAccumulator.ELEMENT_ADDER_NESTED_CONFIG_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT,
                AddingElementLabelAccumulator.ELEMENT_ADDER_NESTED_CONFIG_LABEL);
        
        final ImageDescriptor addImage = RedImages.getAddImage();
        final Image imageToUse = ImagesManager.getImage(isEditable ? addImage : RedImages.getGreyedImage(addImage));

        final TextPainter textPainter = new TextPainter(false, true, 2);
        final ICellPainter cellPainter = new CellPainterDecorator(textPainter, CellEdgeEnum.LEFT,
                new ImagePainter(imageToUse));

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter, DisplayMode.NORMAL,
                AddingElementLabelAccumulator.ELEMENT_ADDER_CONFIG_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, new DropdownPainter(cellPainter),
                DisplayMode.NORMAL, AddingElementLabelAccumulator.ELEMENT_MULTISTATE_ADDER_CONFIG_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, textPainter, DisplayMode.NORMAL,
                AddingElementLabelAccumulator.ELEMENT_ADDER_NESTED_CONFIG_LABEL);
    }

    private Font getFont(final Font fontToReuse, final int style) {
        final Font currentFont = fontToReuse == null ? Display.getCurrent().getSystemFont() : fontToReuse;
        final FontDescriptor fontDescriptor = FontDescriptor.createFrom(currentFont).setStyle(style);
        return FontsManager.getFont(fontDescriptor);
    }

    private static class DropdownPainter extends CellPainterDecorator {

        public DropdownPainter(final ICellPainter cellPainter) {
            super(cellPainter, CellEdgeEnum.RIGHT, new DropdownImagePainter());
        }
    }

    public static class DropdownImagePainter extends ImagePainter {

        public DropdownImagePainter() {
            super(ImagesManager.getImage(RedImages.getAdderStateChangeImage()));
        }
    }
}
