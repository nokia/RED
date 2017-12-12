/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.KeywordDocumentationPopup;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.SourceOpeningSupport;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 *
 */
@SuppressWarnings("restriction")
public class KeywordDocumentationHyperlink implements RedHyperlink {

    private final RobotModel model;

    private final IRegion from;

    private final IProject project;

    private final LibrarySpecification libSpec;

    private final KeywordSpecification kwSpec;


    public KeywordDocumentationHyperlink(final IRegion from, final IProject project, final LibrarySpecification libSpec,
            final KeywordSpecification kwSpec) {
        this(RedPlugin.getModelManager().getModel(), from, project, libSpec, kwSpec);
    }

    @VisibleForTesting
    KeywordDocumentationHyperlink(final RobotModel model, final IRegion from, final IProject project,
            final LibrarySpecification libSpec, final KeywordSpecification kwSpec) {
        this.model = model;
        this.from = from;
        this.project = project;
        this.libSpec = libSpec;
        this.kwSpec = kwSpec;
    }

    @VisibleForTesting
    public LibrarySpecification getDestinationLibrarySpecification() {
        return libSpec;
    }

    @VisibleForTesting
    public KeywordSpecification getDestinationKeywordSpecification() {
        return kwSpec;
    }

    @Override
    public IRegion getHyperlinkRegion() {
        return from;
    }

    @Override
    public String getTypeLabel() {
        return null;
    }

    @Override
    public String getHyperlinkText() {
        return "Open Documentation";
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
        return "[" + SourceOpeningSupport.extractLibraryLocation(model, project, libSpec) + "]";
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getLibraryImage();
    }

    @Override
    public void open() {
        open(kwSpec);
    }

    protected final void open(final KeywordSpecification keywordSpecification) {
        final IWorkbenchWindow workbench = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        final IThemeEngine themeEngine = workbench.getService(IThemeEngine.class);
        final Shell shell = workbench.getShell();
        new KeywordDocumentationPopup(shell, themeEngine, keywordSpecification).open();
    }
}
