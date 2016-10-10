/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.KeywordDocumentationPopup;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.ShowLibrarySourceAction;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

/**
 * @author Michal Anglart
 *
 */
public class KeywordDocumentationHyperlink implements RedHyperlink {

    private final IRegion from;

    private final IProject project;

    private final LibrarySpecification libSpec;

    private final KeywordSpecification kwSpec;

    public KeywordDocumentationHyperlink(final IRegion from, final IProject project, final LibrarySpecification libSpec,
            final KeywordSpecification kwSpec) {
        this.from = from;
        this.project = project;
        this.libSpec = libSpec;
        this.kwSpec = kwSpec;
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
    public String getLabelForCompoundHyperlinksDialog() {
        return libSpec.getName();
    }

    @Override
    public String additionalLabelDecoration() {
        return "[" + ShowLibrarySourceAction.extractLibraryLocation(project, libSpec) + "]";
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getLibraryImage();
    }

    @Override
    public void open() {
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        new KeywordDocumentationPopup(shell, kwSpec).open();
    }
}
