/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.jface.resource.FontDescriptor;
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable.TableThemes.TableTheme;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author Michal Anglart
 *
 */
public class AddingElementConfiguration extends AbstractRegistryConfiguration {

    public static final String ELEMENT_ADDER_CONFIG_LABEL = "ELEMENT_ADDER";

    private final Font font;

    public AddingElementConfiguration(final TableTheme theme) {
        this.font = theme.getFont();
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final Style style = new Style();
        style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, ColorsManager.getColor(30, 127, 60));
        style.setAttributeValue(CellStyleAttributes.FONT, getFont(font, SWT.ITALIC));

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL,
                ELEMENT_ADDER_CONFIG_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT,
                ELEMENT_ADDER_CONFIG_LABEL);

        final ICellPainter cellPainter = new CellPainterDecorator(new TextPainter(false, true, 2), CellEdgeEnum.LEFT,
                new ImagePainter(ImagesManager.getImage(RedImages.getAddImage())));
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter,
                DisplayMode.NORMAL, ELEMENT_ADDER_CONFIG_LABEL);
    }

    private Font getFont(final Font fontToReuse, final int style) {
        final Font currentFont = fontToReuse == null ? Display.getCurrent().getSystemFont() : fontToReuse;
        final FontDescriptor fontDescriptor = FontDescriptor.createFrom(currentFont).setStyle(style);
        return FontsManager.getFont(fontDescriptor);
    }
}