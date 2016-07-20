/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.e4.tools.compat.parts.DIEditorPart;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;

import com.google.common.base.Optional;

public class DISectionEditorPart<C extends ISectionEditorPart> extends DIEditorPart<C> implements ISectionEditorPart {

    public DISectionEditorPart(final Class<C> clazz) {
        super(clazz);
    }

    @Override
    public String getId() {
        return getComponent().getId();
    }

    @Override
    public void updateOnActivation() {
        getComponent().updateOnActivation();
    }

    @Override
    public Optional<? extends RobotSuiteFileSection> provideSection(final RobotSuiteFile suiteModel) {
        return getComponent().provideSection(suiteModel);
    }

    @Override
    public boolean isPartFor(final RobotSuiteFileSection section) {
        return getComponent().isPartFor(section);
    }

    @Override
    public void revealElement(final RobotElement robotElement) {
        getComponent().revealElement(robotElement);
    }

    @Override
    public void setFocus() {
        getComponent().setFocus();
    }

    @Override
    public FocusedViewerAccessor getFocusedViewerAccessor() {
        return getComponent().getFocusedViewerAccessor();
    }

    @Override
    public SelectionLayerAccessor getSelectionLayerAccessor() {
        return getComponent().getSelectionLayerAccessor();
    }
    
    @Override
    public Optional<TreeLayerAccessor> getTreeLayerAccessor() {
        return getComponent().getTreeLayerAccessor();
    }
    
    @Override
    public void waitForPendingJobs() {
        getComponent().waitForPendingJobs();
    }

}
