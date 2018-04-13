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
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.LibraryLocationFinder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.SourceOpeningSupport;

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

    private final KeywordSpecification kwSpec;

    public KeywordInLibrarySourceHyperlink(final IRegion from, final IProject project,
            final LibrarySpecification libSpec, final KeywordSpecification kwSpec) {
        this(RedPlugin.getModelManager().getModel(), from, project, libSpec, kwSpec);
    }

    @VisibleForTesting
    KeywordInLibrarySourceHyperlink(final RobotModel model, final IRegion from, final IProject project,
            final LibrarySpecification libSpec, final KeywordSpecification kwSpec) {
        this.model = model;
        this.source = from;
        this.project = project;
        this.libSpec = libSpec;
        this.kwSpec = kwSpec;
    }

    @VisibleForTesting
    public LibrarySpecification getDestinationSpecification() {
        return libSpec;
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
        return "Open Definition " + elementName();
    }

    @Override
    public String elementName() {
        return kwSpec.getName();
    }

    @Override
    public String getLabelForCompoundHyperlinksDialog() {
        return libSpec.getName();
    }

    @Override
    public String additionalLabelDecoration() {
        return LibraryLocationFinder.findPath(model, project, libSpec).map(path -> "[" + path + "]").orElse("");
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getLibraryImage();
    }

    @Override
    public void open() {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        SourceOpeningSupport.open(page, model, project, libSpec, kwSpec);
    }
}
