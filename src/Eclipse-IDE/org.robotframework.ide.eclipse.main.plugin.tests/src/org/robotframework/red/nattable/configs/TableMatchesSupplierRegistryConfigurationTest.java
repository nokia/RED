/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.function.Supplier;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.red.nattable.ITableStringsDecorationsSupport;

public class TableMatchesSupplierRegistryConfigurationTest {

    @Test
    public void matchesSupplierIsProperlyRegistered() {
        final IConfigRegistry registry = mock(IConfigRegistry.class);

        final Supplier<HeaderFilterMatchesCollection> supplier = () -> null;
        final TableMatchesSupplierRegistryConfiguration config = new TableMatchesSupplierRegistryConfiguration(
                supplier);
        config.configureRegistry(registry);

        verify(registry).registerConfigAttribute(ITableStringsDecorationsSupport.MATCHES_SUPPLIER, supplier,
                DisplayMode.NORMAL);
        verifyNoMoreInteractions(registry);
    }
}
