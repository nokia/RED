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
import org.eclipse.swt.graphics.Color;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.graphics.ColorsManager;

public class AlternatingRowsStyleConfigurationTest {

    @Test
    public void configurationCheck() {
        final Color oddBgColorInUse = ColorsManager.getColor(200, 200, 200);
        final Color evenBgColorInUse = ColorsManager.getColor(100, 100, 100);

        final TableTheme theme = mock(TableTheme.class);
        when(theme.getBodyOddRowBackground()).thenReturn(oddBgColorInUse);
        when(theme.getBodyEvenRowBackground()).thenReturn(evenBgColorInUse);

        final AlternatingRowsStyleConfiguration configuration = new AlternatingRowsStyleConfiguration(theme);

        assertThat(configuration).has(oddBackground(oddBgColorInUse));
        assertThat(configuration).has(evenBackground(evenBgColorInUse));
    }

    private Condition<AlternatingRowsStyleConfiguration> oddBackground(final Color bgColorInUse) {
        return new Condition<AlternatingRowsStyleConfiguration>() {

            @Override
            public boolean matches(final AlternatingRowsStyleConfiguration config) {
                return config.oddRowBgColor.equals(bgColorInUse);
            }
        };
    }

    private Condition<AlternatingRowsStyleConfiguration> evenBackground(final Color fgColorInUse) {
        return new Condition<AlternatingRowsStyleConfiguration>() {

            @Override
            public boolean matches(final AlternatingRowsStyleConfiguration config) {
                return config.evenRowBgColor.equals(fgColorInUse);
            }
        };
    }
}
