/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

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
        Shell current = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        while (current != null && current.getParent() instanceof Shell) {
            current = (Shell) current.getParent();
        }

        if (current == null) {
            RedPlugin.logWarning("The parent shell was null when creating hyperlinks dialog");
        }
        new HyperlinkDialog(current, name, hyperlinks).open();
    }
}
