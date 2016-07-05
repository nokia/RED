/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;

public interface ISettingsFormFragment {

    ISelectionProvider getSelectionProvider();

    SelectionLayerAccessor getSelectionLayerAccessor();

    NatTable getTable();

    void invokeSaveAction();
}
