/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.FontsManager;

/**
 * @author Michal Anglart
 *
 */
public class SelectionStyleConfiguration extends DefaultSelectionStyleConfiguration {

    public SelectionStyleConfiguration(final TableTheme theme, final Font fontInUse) {
        selectionFont = FontsManager.getFont(FontDescriptor.createFrom(fontInUse));
        selectionBgColor = theme.getBodySelectedCellBackground();
        selectionFgColor = theme.getBodyForeground();
        selectedHeaderFont = FontsManager.getFont(FontDescriptor.createFrom(fontInUse));
        selectedHeaderBorderStyle = new BorderStyle(0, ColorsManager.getColor(SWT.COLOR_DARK_GRAY),
                LineStyleEnum.SOLID);
        selectedHeaderBgColor = theme.getHeadersHighlightedBackground();
        selectedHeaderFgColor = theme.getBodyForeground();
        anchorBgColor = theme.getBodyAnchoredCellBackground();
        anchorFgColor = null;
        anchorBorderStyle = new BorderStyle(1, theme.getBodySelectionGridColor(), LineStyleEnum.SOLID);
        anchorGridBorderStyle = new BorderStyle(1, theme.getBodySelectionGridColor(), LineStyleEnum.SOLID);
    }
}
