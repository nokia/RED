/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable;

import java.util.function.Function;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;

import com.google.common.base.Supplier;
import com.google.common.collect.RangeMap;

public interface ITableStringsDecorationsSupport {

    ConfigAttribute<TableCellsStrings> TABLE_STRINGS = new ConfigAttribute<>();

    ConfigAttribute<Supplier<HeaderFilterMatchesCollection>> MATCHES_SUPPLIER = new ConfigAttribute<>();

    ConfigAttribute<Function<String, RangeMap<Integer, Styler>>> RANGES_STYLES = new ConfigAttribute<>();
}
