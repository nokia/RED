/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IMarkerResolution;
import org.rf.ide.core.project.RobotProjectConfig;

/**
 * @author Michal Anglart
 *
 */
public abstract class RedXmlConfigMarkerResolution implements IMarkerResolution {

    @Override
    public final void run(final IMarker marker) {
        final IFile file = marker.getResource().getProject().getFile(RobotProjectConfig.FILENAME);
        asContentProposal(marker, file).apply(null);
    }

    public ICompletionProposal asContentProposal(final IMarker marker) {
        final IFile file = marker.getResource().getProject().getFile(RobotProjectConfig.FILENAME);
        return asContentProposal(marker, file);
    }

    protected abstract ICompletionProposal asContentProposal(final IMarker marker, final IFile externalFile);

}
