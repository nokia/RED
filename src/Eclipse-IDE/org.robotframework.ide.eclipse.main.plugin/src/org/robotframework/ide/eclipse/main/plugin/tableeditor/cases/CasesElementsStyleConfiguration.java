/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.InactiveCellPainter;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.nattable.painter.SearchMatchesTextPainter;

import com.google.common.base.Supplier;

public class CasesElementsStyleConfiguration extends AbstractRegistryConfiguration {

    private final Font font;

    private final boolean isEditable;

    private final Supplier<HeaderFilterMatchesCollection> matchesSupplier;

    public CasesElementsStyleConfiguration(final TableTheme theme, final boolean isEditable,
            final Supplier<HeaderFilterMatchesCollection> matchesSupplier) {
        this.font = theme.getFont();
        this.isEditable = isEditable;
        this.matchesSupplier = matchesSupplier;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
        final Style caseStyle = createStyle(preferences, SyntaxHighlightingCategory.DEFINITION);
        final Style settingStyle = createStyle(preferences, SyntaxHighlightingCategory.SETTING);
        // final Style callStyle = createStyle(preferences,
        // SyntaxHighlightingCategory.KEYWORD_CALL);
        
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, caseStyle, DisplayMode.NORMAL,
                CasesElementsLabelAccumulator.CASE_CONFIG_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, caseStyle, DisplayMode.SELECT,
                CasesElementsLabelAccumulator.CASE_CONFIG_LABEL);
        
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, caseStyle, DisplayMode.NORMAL,
                CasesElementsLabelAccumulator.CASE_WITH_TEMPLATE_CONFIG_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, caseStyle, DisplayMode.SELECT,
                CasesElementsLabelAccumulator.CASE_WITH_TEMPLATE_CONFIG_LABEL);
        
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, settingStyle, DisplayMode.NORMAL,
                CasesElementsLabelAccumulator.CASE_SETTING_CONFIG_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, settingStyle, DisplayMode.SELECT,
                CasesElementsLabelAccumulator.CASE_SETTING_CONFIG_LABEL);
        // configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, callStyle,
        // DisplayMode.NORMAL,
        // CasesElementsInTreeLabelAccumulator.CASE_CALL_CONFIG_LABEL);
        // configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, callStyle,
        // DisplayMode.SELECT,
        // CasesElementsInTreeLabelAccumulator.CASE_CALL_CONFIG_LABEL);
        
        final ImageDescriptor caseImage = isEditable ? RedImages.getTestCaseImage()
                : RedImages.getGreyedImage(RedImages.getTestCaseImage());
        final ImageDescriptor templatedCaseImage = isEditable ? RedImages.getTemplatedTestCaseImage()
                : RedImages.getGreyedImage(RedImages.getTemplatedTestCaseImage());
        final ICellPainter caseCellPainter = new CellPainterDecorator(new SearchMatchesTextPainter(matchesSupplier, 2),
                CellEdgeEnum.LEFT, new ImagePainter(ImagesManager.getImage(caseImage)));
        final ICellPainter templatedCaseCellPainter = new CellPainterDecorator(
                new SearchMatchesTextPainter(matchesSupplier, 2), CellEdgeEnum.LEFT,
                new ImagePainter(ImagesManager.getImage(templatedCaseImage)));
        
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, caseCellPainter, DisplayMode.NORMAL,
                CasesElementsLabelAccumulator.CASE_CONFIG_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, templatedCaseCellPainter,
                DisplayMode.NORMAL, CasesElementsLabelAccumulator.CASE_WITH_TEMPLATE_CONFIG_LABEL);
        
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, new InactiveCellPainter(),
                DisplayMode.NORMAL, CasesElementsLabelAccumulator.CELL_NOT_EDITABLE_LABEL);
    }

    private Style createStyle(final RedPreferences preferences, final SyntaxHighlightingCategory category) {
        final Style style = new Style();
        final ColoringPreference syntaxColoring = preferences.getSyntaxColoring(category);
        style.setAttributeValue(CellStyleAttributes.FONT, getFont(font, syntaxColoring.getFontStyle()));
        style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, syntaxColoring.getColor());
        return style;
    }

    private Font getFont(final Font fontToReuse, final int style) {
        final Font currentFont = fontToReuse == null ? Display.getCurrent().getSystemFont() : fontToReuse;
        final FontDescriptor fontDescriptor = FontDescriptor.createFrom(currentFont).setStyle(style);
        return FontsManager.getFont(fontDescriptor);
    }
}
