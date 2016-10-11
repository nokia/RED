/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.ShowLibrarySourceAction;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 *
 */
public class KeywordInLibrarySourceHyperlink implements RedHyperlink {

    private final RobotModel model;

    private final IRegion source;

    private final IProject project;

    private final LibrarySpecification libSpec;

    public KeywordInLibrarySourceHyperlink(final IRegion from, final IProject project, final LibrarySpecification libSpec) {
        this(RedPlugin.getModelManager().getModel(), from, project, libSpec);
    }

    @VisibleForTesting
    KeywordInLibrarySourceHyperlink(final RobotModel model, final IRegion from, final IProject project,
            final LibrarySpecification libSpec) {
        this.model = model;
        this.source = from;
        this.project = project;
        this.libSpec = libSpec;
    }

    @Override
    public IRegion getHyperlinkRegion() {
        return source;
    }

    @Override
    public String getTypeLabel() {
        return null;
    }

    @Override
    public String getHyperlinkText() {
        return "Open Definition";
    }

    @Override
    public String getLabelForCompoundHyperlinksDialog() {
        return libSpec.getName();
    }

    @Override
    public String additionalLabelDecoration() {
        return "[" + ShowLibrarySourceAction.extractLibraryLocation(model, project, libSpec) + "]";
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getLibraryImage();
    }

    @Override
    public void open() {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        ShowLibrarySourceAction.openLibrarySource(page, model, project, libSpec);
    }
}
