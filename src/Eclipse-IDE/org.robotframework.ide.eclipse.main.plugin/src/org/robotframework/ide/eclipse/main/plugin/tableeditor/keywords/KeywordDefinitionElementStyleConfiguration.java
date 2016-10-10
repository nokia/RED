/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.InactiveCellPainter;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.nattable.painter.RedTableTextPainter;

public class KeywordDefinitionElementStyleConfiguration extends AbstractRegistryConfiguration {

    private final Font font;

    private final boolean isEditable;

    public KeywordDefinitionElementStyleConfiguration(final TableTheme theme, final boolean isEditable) {
        this.font = theme.getFont();
        this.isEditable = isEditable;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final Style keywordStyle = new Style();
        final Style argumentStyle = new Style();
        final Style settingStyle = new Style();

        final Color argumentForegroundColor = isEditable ? ColorsManager.getColor(30, 127, 60)
                : ColorsManager.getColor(200, 200, 200);
        argumentStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, argumentForegroundColor);
        final Color settingForegroundColor = isEditable ? ColorsManager.getColor(149, 0, 85)
                : ColorsManager.getColor(200, 200, 200);
        settingStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, settingForegroundColor);
        keywordStyle.setAttributeValue(CellStyleAttributes.FONT, getFont(font, SWT.BOLD));

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, keywordStyle, DisplayMode.NORMAL,
                KeywordElementsInTreeLabelAccumulator.KEYWORD_DEFINITION_CONFIG_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, keywordStyle, DisplayMode.SELECT,
                KeywordElementsInTreeLabelAccumulator.KEYWORD_DEFINITION_CONFIG_LABEL);

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, argumentStyle, DisplayMode.NORMAL,
                KeywordElementsInTreeLabelAccumulator.KEYWORD_DEFINITION_ARGUMENT_CONFIG_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, argumentStyle, DisplayMode.SELECT,
                KeywordElementsInTreeLabelAccumulator.KEYWORD_DEFINITION_ARGUMENT_CONFIG_LABEL);

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, settingStyle, DisplayMode.NORMAL,
                KeywordElementsInTreeLabelAccumulator.KEYWORD_DEFINITION_SETTING_CONFIG_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, settingStyle, DisplayMode.SELECT,
                KeywordElementsInTreeLabelAccumulator.KEYWORD_DEFINITION_SETTING_CONFIG_LABEL);

        final ImageDescriptor keywordImage = RedImages.getUserKeywordImage();
        final Image imageToUse = ImagesManager
                .getImage(isEditable ? keywordImage : RedImages.getGreyedImage(keywordImage));

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, new InactiveCellPainter(),
                DisplayMode.NORMAL,
                KeywordElementsInTreeLabelAccumulator.KEYWORD_DEFINITION_SETTING_DOCUMENTATION_NOT_EDITABLE_LABEL);

        final ICellPainter cellPainter = new CellPainterDecorator(new RedTableTextPainter(2), CellEdgeEnum.LEFT,
                new ImagePainter(imageToUse));
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter, DisplayMode.NORMAL,
                KeywordElementsInTreeLabelAccumulator.KEYWORD_DEFINITION_CONFIG_LABEL);
    };

    private Font getFont(final Font fontToReuse, final int style) {
        final Font currentFont = fontToReuse == null ? Display.getCurrent().getSystemFont() : fontToReuse;
        final FontDescriptor fontDescriptor = FontDescriptor.createFrom(currentFont).setStyle(style);
        return FontsManager.getFont(fontDescriptor);
    }

}
