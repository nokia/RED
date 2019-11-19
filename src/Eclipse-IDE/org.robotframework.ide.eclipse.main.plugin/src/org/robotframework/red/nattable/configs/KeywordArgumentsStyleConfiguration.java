/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.stream.Stream;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;

public class KeywordArgumentsStyleConfiguration extends AbstractRegistryConfiguration {

    private final TableTheme theme;

    public KeywordArgumentsStyleConfiguration(final TableTheme theme) {
        this.theme = theme;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final Style missingArgumentStyle = createMissingArgumentStyle();
        final Style optionalArgumentStyle = createOptionalArgumentStyle();
        final Style redundantArgumentStyle = createRedundantArgumentStyle();

        Stream.of(DisplayMode.NORMAL, DisplayMode.SELECT).forEach(mode -> {
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, missingArgumentStyle, mode,
                    TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, optionalArgumentStyle, mode,
                    TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, redundantArgumentStyle, mode,
                    TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        });

    }

    private Style createMissingArgumentStyle() {
        final Style style = new Style();
        style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, theme.getBodyMissingArgumentCellBackground());
        return style;
    }

    private Style createOptionalArgumentStyle() {
        final Style style = new Style();
        style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, theme.getBodyOptionalArgumentCellBackground());
        return style;
    }

    private Style createRedundantArgumentStyle() {
        final Style style = new Style();
        style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, theme.getBodyInactiveCellBackground());
        return style;
    }
}