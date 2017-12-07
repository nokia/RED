/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

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
import org.robotframework.red.nattable.painter.InactiveCellPainter;
import org.robotframework.red.nattable.painter.RedTableTextPainter;

public class KeywordsElementsStyleConfiguration extends AbstractRegistryConfiguration {

    private final TableTheme theme;

    private final boolean isEditable;

    private final boolean wrapCellContent;

    public KeywordsElementsStyleConfiguration(final TableTheme theme, final boolean isEditable,
            final boolean wrapCellContent) {
        this.theme = theme;
        this.isEditable = isEditable;
        this.wrapCellContent = wrapCellContent;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();

        final Style keywordStyle = createStyle(preferences, SyntaxHighlightingCategory.DEFINITION);
        final Style argumentStyle = createStyle(preferences, SyntaxHighlightingCategory.VARIABLE);
        final Style settingStyle = createStyle(preferences, SyntaxHighlightingCategory.SETTING);

        Stream.of(DisplayMode.NORMAL, DisplayMode.HOVER, DisplayMode.SELECT, DisplayMode.SELECT_HOVER).forEach(mode -> {
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, keywordStyle, mode,
                    KeywordsElementsLabelAccumulator.KEYWORD_DEFINITION_CONFIG_LABEL);
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, argumentStyle, mode,
                    KeywordsElementsLabelAccumulator.KEYWORD_DEFINITION_ARGUMENT_CONFIG_LABEL);
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, settingStyle, mode,
                    KeywordsElementsLabelAccumulator.KEYWORD_DEFINITION_SETTING_CONFIG_LABEL);
        });

        final ImageDescriptor keywordImage = isEditable ? RedImages.getUserKeywordImage()
                : RedImages.getGrayedImage(RedImages.getUserKeywordImage());
        final ICellPainter cellPainter = new CellPainterDecorator(new RedTableTextPainter(wrapCellContent, 2),
                CellEdgeEnum.LEFT, new ImagePainter(ImagesManager.getImage(keywordImage)));
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter, DisplayMode.NORMAL,
                KeywordsElementsLabelAccumulator.KEYWORD_DEFINITION_CONFIG_LABEL);

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
                new InactiveCellPainter(theme.getBodyInactiveCellBackground()), DisplayMode.NORMAL,
                TableConfigurationLabels.CELL_NOT_EDITABLE_LABEL);
    }

    private Style createStyle(final RedPreferences preferences, final SyntaxHighlightingCategory category) {
        final Style style = new Style();
        final ColoringPreference syntaxColoring = preferences.getSyntaxColoring(category);
        style.setAttributeValue(CellStyleAttributes.FONT,
                FontsManager.transformFontWithStyle(theme.getFont(), syntaxColoring.getFontStyle()));
        style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, syntaxColoring.getColor());
        return style;
    }
}
