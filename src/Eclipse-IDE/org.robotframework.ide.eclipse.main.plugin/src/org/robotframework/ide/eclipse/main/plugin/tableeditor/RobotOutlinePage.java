/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.navigator.NavigatorLabelProvider;

class RobotOutlinePage extends ContentOutlinePage {

    private final RobotSuiteFile suiteModel;

    public RobotOutlinePage(final RobotSuiteFile suiteModel) {
        this.suiteModel = suiteModel;
    }

    @Override
    public void createControl(final Composite parent) {
        super.createControl(parent);

        getTreeViewer().setContentProvider(new RobotOutlineContentProvider());
        ViewerColumnsFactory.newColumn("")
            .withWidth(400)
            .labelsProvidedBy(new NavigatorLabelProvider())
            .createFor(getTreeViewer());

        getTreeViewer().setInput(new Object[] { suiteModel });

        getTreeViewer().expandToLevel(3);
    }

}
