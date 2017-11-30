/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Condition;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.swt.graphics.Color;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.graphics.ColorsManager;

public class GeneralTableStyleConfigurationTest {

    @Test
    public void configurationCheck() {
        final Color bgColorInUse = ColorsManager.getColor(200, 200, 200);
        final Color fgColorInUse = ColorsManager.getColor(100, 100, 100);

        final TableTheme theme = mock(TableTheme.class);
        when(theme.getBodyOddRowBackground()).thenReturn(bgColorInUse);
        when(theme.getBodyForeground()).thenReturn(fgColorInUse);

        final GeneralTableStyleConfiguration configuration = new GeneralTableStyleConfiguration(theme, mock(TextPainter.class));

        assertThat(configuration).has(background(bgColorInUse));
        assertThat(configuration).has(foreground(fgColorInUse));
        assertThat(configuration.hAlign).isEqualTo(HorizontalAlignmentEnum.LEFT);
        assertThat(configuration.vAlign).isEqualTo(VerticalAlignmentEnum.TOP);
        assertThat(configuration.cellPainter).isInstanceOf(PaddingDecorator.class);
    }

    private Condition<GeneralTableStyleConfiguration> background(final Color bgColorInUse) {
        return new Condition<GeneralTableStyleConfiguration>() {

            @Override
            public boolean matches(final GeneralTableStyleConfiguration config) {
                return config.bgColor.equals(bgColorInUse);
            }
        };
    }

    private Condition<GeneralTableStyleConfiguration> foreground(final Color fgColorInUse) {
        return new Condition<GeneralTableStyleConfiguration>() {

            @Override
            public boolean matches(final GeneralTableStyleConfiguration config) {
                return config.fgColor.equals(fgColorInUse);
            }
        };
    }
}
