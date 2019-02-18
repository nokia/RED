/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.stream.Stream;

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
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.nattable.painter.RedTableTextPainter;

public class CasesElementsStyleConfiguration extends AbstractRegistryConfiguration {

    private final TableTheme theme;

    private final boolean isEditable;

    private final boolean wrapCellContent;

    private final ImageDescriptor caseImage;

    private final ImageDescriptor templatedCaseImage;

    public CasesElementsStyleConfiguration(final TableTheme theme, final boolean isEditable,
            final boolean wrapCellContent, final ImageDescriptor caseImage, final ImageDescriptor templatedCaseImage) {
        this.theme = theme;
        this.isEditable = isEditable;
        this.wrapCellContent = wrapCellContent;
        this.caseImage = caseImage;
        this.templatedCaseImage = templatedCaseImage;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();

        final Style caseStyle = createStyle(preferences, SyntaxHighlightingCategory.DEFINITION);
        final Style settingStyle = createStyle(preferences, SyntaxHighlightingCategory.SETTING);
        final Style notEditableStyle = createNonEditableStyle();
        
        Stream.of(DisplayMode.NORMAL, DisplayMode.HOVER, DisplayMode.SELECT, DisplayMode.SELECT_HOVER).forEach(mode -> {
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, caseStyle, mode,
                    CasesElementsLabelAccumulator.CASE_CONFIG_LABEL);
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, caseStyle, mode,
                    CasesElementsLabelAccumulator.CASE_WITH_TEMPLATE_CONFIG_LABEL);
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, settingStyle, mode,
                    CasesElementsLabelAccumulator.CASE_SETTING_CONFIG_LABEL);
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, notEditableStyle, mode,
                    TableConfigurationLabels.CELL_NOT_EDITABLE_LABEL);
        });

        final ImageDescriptor caseImg = isEditable ? caseImage : RedImages.getGrayedImage(caseImage);
        final ImageDescriptor templatedCaseImg = isEditable ? templatedCaseImage
                : RedImages.getGrayedImage(templatedCaseImage);
        final ICellPainter caseCellPainter = new CellPainterDecorator(new RedTableTextPainter(wrapCellContent, 2),
                CellEdgeEnum.LEFT, new ImagePainter(ImagesManager.getImage(caseImg)));
        final ICellPainter templatedCaseCellPainter = new CellPainterDecorator(
                new RedTableTextPainter(wrapCellContent, 2), CellEdgeEnum.LEFT,
                new ImagePainter(ImagesManager.getImage(templatedCaseImg)));

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, caseCellPainter, DisplayMode.NORMAL,
                CasesElementsLabelAccumulator.CASE_CONFIG_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, templatedCaseCellPainter,
                DisplayMode.NORMAL, CasesElementsLabelAccumulator.CASE_WITH_TEMPLATE_CONFIG_LABEL);
    }

    private Style createStyle(final RedPreferences preferences, final SyntaxHighlightingCategory category) {
        final Style style = new Style();
        final ColoringPreference syntaxColoring = preferences.getSyntaxColoring(category);
        style.setAttributeValue(CellStyleAttributes.FONT,
                FontsManager.transformFontWithStyle(theme.getFont(), syntaxColoring.getFontStyle()));
        style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, syntaxColoring.getColor());
        return style;
    }

    private Style createNonEditableStyle() {
        final Style notEditableStyle = new Style();
        notEditableStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, theme.getBodyInactiveCellBackground());
        return notEditableStyle;
    }
}
