/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class RunTestFromTableInDebugDynamicMenuItem extends RunTestFromTableDynamicMenuItem {

    public RunTestFromTableInDebugDynamicMenuItem() {
        super("org.robotframework.red.menu.dynamic.table.debug");
    }

    @Override
    protected void contributeBefore(final List<IContributionItem> contributedItems) {
        // nothing to contribute
    }

    @Override
    protected String getModeName() {
        return "Debug";
    }

    @Override
    protected ImageDescriptor getImageDescriptor() {
        return RedImages.getExecuteDebugImage();
    }
}
