/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

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
    public Set<String> getDetailPaneTypes(IStructuredSelection selection) {
        Set<String> typeSet = new HashSet<String>();
        if (selection != null && !selection.isEmpty() && selection.getFirstElement() instanceof RobotLineBreakpoint) {
            typeSet.add(BreakpointDetailPane.BREAKPOINT_DETAIL_PANE_ID);
        }

        return typeSet;
    }

    @Override
    public String getDefaultDetailPane(IStructuredSelection selection) {
        return null;
    }

    @Override
    public IDetailPane createDetailPane(String paneID) {
        if (BreakpointDetailPane.BREAKPOINT_DETAIL_PANE_ID.equals(paneID)) {
            return new BreakpointDetailPane();
        }
        return null;
    }

    @Override
    public String getDetailPaneName(String paneID) {
        return "Detail Pane Name";
    }

    @Override
    public String getDetailPaneDescription(String paneID) {
        return "Detail Pane Description";
    }

}
