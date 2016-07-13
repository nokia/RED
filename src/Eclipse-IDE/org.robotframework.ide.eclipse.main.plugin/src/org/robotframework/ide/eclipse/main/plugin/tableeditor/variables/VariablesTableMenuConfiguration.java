/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.ui.IEditorSite;
import org.robotframework.red.nattable.configs.TableMenuConfiguration;

/**
 * @author Michal Anglart
 *
 */
public class VariablesTableMenuConfiguration extends TableMenuConfiguration {

    public VariablesTableMenuConfiguration(final IEditorSite site, final NatTable table,
            final ISelectionProvider selectionProvider) {
        super(site, table, selectionProvider, "org.robotframework.ide.eclipse.editor.page.variables.contextMenu",
                "Robot suite editor variables page context menu");
    }
}
