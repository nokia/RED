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
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotKeywordFailBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;

/**
 * @author mmarzec
 *
 */
public class BreakpointDetailPaneFactory implements IDetailPaneFactory {

    @Override
    public Set<String> getDetailPaneTypes(final IStructuredSelection selection) {
        if (selection != null && selection.size() == 1) {
            if (selection.getFirstElement() instanceof RobotLineBreakpoint) {
                return newHashSet(RobotLineBreakpointDetailPane.ID);

            } else if (selection.getFirstElement() instanceof RobotKeywordFailBreakpoint) {
                return newHashSet(RobotKeywordFailBreakpointDetailPane.ID);
            }
        }
        return new HashSet<>();
    }

    @Override
    public String getDefaultDetailPane(final IStructuredSelection selection) {
        return null;
    }

    @Override
    public IDetailPane createDetailPane(final String paneId) {
        if (RobotLineBreakpointDetailPane.ID.equals(paneId)) {
            return new RobotLineBreakpointDetailPane();

        } else if (RobotKeywordFailBreakpointDetailPane.ID.equals(paneId)) {
            return new RobotKeywordFailBreakpointDetailPane();
        }
        return null;
    }

    @Override
    public String getDetailPaneName(final String paneId) {
        if (RobotLineBreakpointDetailPane.ID.equals(paneId)) {
            return RobotLineBreakpointDetailPane.NAME;
        } else if (RobotKeywordFailBreakpointDetailPane.ID.equals(paneId)) {
            return RobotKeywordFailBreakpointDetailPane.NAME;
        }
        return null;
    }

    @Override
    public String getDetailPaneDescription(final String paneId) {
        if (RobotLineBreakpointDetailPane.ID.equals(paneId)) {
            return RobotLineBreakpointDetailPane.DESCRIPTION;

        } else if (RobotKeywordFailBreakpointDetailPane.ID.equals(paneId)) {
            return RobotKeywordFailBreakpointDetailPane.DESCRIPTION;
        }
        return null;
    }
}
