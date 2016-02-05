/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.hyperlinks;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.KeywordDocumentationPopup;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

/**
 * @author Michal Anglart
 *
 */
public class KeywordDocumentationHyperlink implements RedHyperlink {

    private final IRegion source;

    private final LibrarySpecification libSpec;

    private final KeywordSpecification kwSpec;

    public KeywordDocumentationHyperlink(final IRegion from, final LibrarySpecification libSpec,
            final KeywordSpecification kwSpec) {
        this.source = from;
        this.libSpec = libSpec;
        this.kwSpec = kwSpec;
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
        return "Open Documentation";
    }

    @Override
    public String getLabelForCompoundHyperlinksDialog() {
        return libSpec.getName();
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
