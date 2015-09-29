/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.hyperlinks;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.KeywordDocumentationPopup;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;

/**
 * @author Michal Anglart
 *
 */
public class LibraryKeywordHyperlink implements IHyperlink {

    private final IRegion source;

    private final KeywordSpecification kwSpec;

    public LibraryKeywordHyperlink(final IRegion from, final KeywordSpecification kwSpec) {
        this.source = from;
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
        return null;
    }

    @Override
    public void open() {
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        new KeywordDocumentationPopup(shell, kwSpec).open();
    }
}
