/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.hyperlinks;

import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.PlatformUI;


/**
 * @author Michal Anglart
 *
 */
public class CompoundHyperlink implements IHyperlink {

    private final IRegion source;

    private final List<RedHyperlink> hyperlinks;

    private final String name;

    private final String label;

    public CompoundHyperlink(final String name, final IRegion sourceRegion, final List<RedHyperlink> hyperlinks,
            final String label) {
        this.name = name;
        this.source = sourceRegion;
        this.hyperlinks = hyperlinks;
        this.label = label;
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
        final HyperlinkDialog dialog = new HyperlinkDialog(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), name, hyperlinks);
        dialog.open();
    }

}
