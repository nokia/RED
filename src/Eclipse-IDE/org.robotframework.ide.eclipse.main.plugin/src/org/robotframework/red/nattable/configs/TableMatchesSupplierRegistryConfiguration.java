/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.red.nattable.ITableStringsDecorationsSupport;

import com.google.common.base.Supplier;


public class TableMatchesSupplierRegistryConfiguration extends AbstractRegistryConfiguration {

    private final Supplier<HeaderFilterMatchesCollection> matchesSupplier;

    public TableMatchesSupplierRegistryConfiguration(final Supplier<HeaderFilterMatchesCollection> matchesSupplier) {
        this.matchesSupplier = matchesSupplier;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(ITableStringsDecorationsSupport.MATCHES_SUPPLIER, matchesSupplier,
                DisplayMode.NORMAL);
    }
}
