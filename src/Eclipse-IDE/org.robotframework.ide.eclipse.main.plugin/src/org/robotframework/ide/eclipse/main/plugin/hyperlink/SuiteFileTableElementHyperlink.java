/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement.OpenStrategy;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;


public class SuiteFileTableElementHyperlink extends RedClipboard implements RedHyperlink {

    private final IRegion from;

    private final RobotSuiteFile destinationFile;

    private final RobotFileInternalElement targetElement;

    private final String labelPart;

    public SuiteFileTableElementHyperlink(final String labelPart, final IRegion from, final RobotSuiteFile toFile,
            final RobotFileInternalElement targetElement) {
        this.labelPart = labelPart;
        this.from = from;
        this.destinationFile = toFile;
        this.targetElement = targetElement;
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
        return "Open Definition";
    }

    @Override
    public String getLabelForCompoundHyperlinksDialog() {
        return destinationFile.getName();
    }

    @Override
    public String additionalLabelDecoration() {
        return "";
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getImageForFileWithExtension(destinationFile.getFileExtension());
    }

    @Override
    public void open() {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final OpenStrategy openStrategy = targetElement.getOpenRobotEditorStrategy(page);
        if (openStrategy != null) {
            openStrategy.run(labelPart);
        }
    }
}
