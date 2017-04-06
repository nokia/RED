/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import static com.google.common.collect.Sets.newHashSet;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;

/**
 * @author mmarzec
 *
 */
public class BreakpointDetailPaneFactory implements IDetailPaneFactory {

    @Override
    public Set<String> getDetailPaneTypes(final IStructuredSelection selection) {
        if (selection != null && selection.size() == 1 && selection.getFirstElement() instanceof RobotLineBreakpoint) {
            return newHashSet(BreakpointDetailPane.ID);
        }
        return new HashSet<>();
    }

    @Override
    public String getDefaultDetailPane(final IStructuredSelection selection) {
        return null;
    }

    @Override
    public IDetailPane createDetailPane(final String paneID) {
        if (BreakpointDetailPane.ID.equals(paneID)) {
            return new BreakpointDetailPane();
        }
        return null;
    }

    @Override
    public String getDetailPaneName(final String paneID) {
        if (BreakpointDetailPane.ID.equals(paneID)) {
            return BreakpointDetailPane.NAME;
        }
        return null;
    }

    @Override
    public String getDetailPaneDescription(final String paneID) {
        if (BreakpointDetailPane.ID.equals(paneID)) {
            return BreakpointDetailPane.DESCRIPTION;
        }
        return null;
    }
}
