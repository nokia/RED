/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import java.util.function.Function;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.SourceOpeningSupport;

/**
 * @author Michal Anglart
 */
public class FileHyperlink implements IHyperlink {

    private final IRegion source;

    private final IFile destinationFile;

    private final String label;

    private final Function<IFile, Void> operationToPerformAfterOpening;

    public FileHyperlink(final IRegion from, final IFile toFile, final String label,
            final Function<IFile, Void> operationToPerformAfterOpening) {
        this.source = from;
        this.destinationFile = toFile;
        this.label = label;
        this.operationToPerformAfterOpening = operationToPerformAfterOpening;
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
        return label;
    }

    @Override
    public void open() {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        SourceOpeningSupport.tryToOpenInEditor(page, destinationFile);
        operationToPerformAfterOpening.apply(destinationFile);
    }
}
