/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable;

import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;

import com.google.common.base.Supplier;

public interface ITableStringsDecorationsSupport {

    ConfigAttribute<TableCellsStrings> TABLE_STRINGS = new ConfigAttribute<>();

    ConfigAttribute<Supplier<HeaderFilterMatchesCollection>> MATCHES_SUPPLIER = new ConfigAttribute<>();
}
