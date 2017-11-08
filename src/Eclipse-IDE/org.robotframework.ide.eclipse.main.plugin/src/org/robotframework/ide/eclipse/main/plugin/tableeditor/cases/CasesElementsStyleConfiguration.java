/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.stream.Stream;

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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.nattable.painter.InactiveCellPainter;
import org.robotframework.red.nattable.painter.RedTableTextPainter;

class CasesElementsStyleConfiguration extends AbstractRegistryConfiguration {

    private final TableTheme theme;

    private final boolean isEditable;

    private final boolean wrapCellContent;

    CasesElementsStyleConfiguration(final TableTheme theme, final boolean isEditable, final boolean wrapCellContent) {
        this.theme = theme;
        this.isEditable = isEditable;
        this.wrapCellContent = wrapCellContent;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();

        final Style caseStyle = createStyle(preferences, SyntaxHighlightingCategory.DEFINITION);
        final Style settingStyle = createStyle(preferences, SyntaxHighlightingCategory.SETTING);
        
        Stream.of(DisplayMode.NORMAL, DisplayMode.HOVER, DisplayMode.SELECT, DisplayMode.SELECT_HOVER).forEach(mode -> {
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, caseStyle, mode,
                    CasesElementsLabelAccumulator.CASE_CONFIG_LABEL);
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, caseStyle, mode,
                    CasesElementsLabelAccumulator.CASE_WITH_TEMPLATE_CONFIG_LABEL);
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, settingStyle, mode,
                    CasesElementsLabelAccumulator.CASE_SETTING_CONFIG_LABEL);
        });

        final ImageDescriptor caseImage = isEditable ? RedImages.getTestCaseImage()
                : RedImages.getGrayedImage(RedImages.getTestCaseImage());
        final ImageDescriptor templatedCaseImage = isEditable ? RedImages.getTemplatedTestCaseImage()
                : RedImages.getGrayedImage(RedImages.getTemplatedTestCaseImage());
        final ICellPainter caseCellPainter = new CellPainterDecorator(new RedTableTextPainter(wrapCellContent, 2),
                CellEdgeEnum.LEFT, new ImagePainter(ImagesManager.getImage(caseImage)));
        final ICellPainter templatedCaseCellPainter = new CellPainterDecorator(
                new RedTableTextPainter(wrapCellContent, 2), CellEdgeEnum.LEFT,
                new ImagePainter(ImagesManager.getImage(templatedCaseImage)));
        
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, caseCellPainter, DisplayMode.NORMAL,
                CasesElementsLabelAccumulator.CASE_CONFIG_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, templatedCaseCellPainter,
                DisplayMode.NORMAL, CasesElementsLabelAccumulator.CASE_WITH_TEMPLATE_CONFIG_LABEL);
        
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
                new InactiveCellPainter(theme.getBodyInactiveCellBackground()), DisplayMode.NORMAL,
                TableConfigurationLabels.CELL_NOT_EDITABLE_LABEL);
    }

    private Style createStyle(final RedPreferences preferences, final SyntaxHighlightingCategory category) {
        final Style style = new Style();
        final ColoringPreference syntaxColoring = preferences.getSyntaxColoring(category);
        style.setAttributeValue(CellStyleAttributes.FONT, getFont(theme.getFont(), syntaxColoring.getFontStyle()));
        style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, syntaxColoring.getColor());
        return style;
    }

    private Font getFont(final Font fontToReuse, final int style) {
        final Font currentFont = fontToReuse == null ? Display.getCurrent().getSystemFont() : fontToReuse;
        final FontDescriptor fontDescriptor = FontDescriptor.createFrom(currentFont).setStyle(style);
        return FontsManager.getFont(fontDescriptor);
    }
}
