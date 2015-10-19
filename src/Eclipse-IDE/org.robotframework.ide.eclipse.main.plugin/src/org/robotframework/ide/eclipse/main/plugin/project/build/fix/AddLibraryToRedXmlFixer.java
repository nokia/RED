/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.CannotReadProjectConfigurationException;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigWriter;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigWriter.CannotWriteProjectConfigurationException;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class AddLibraryToRedXmlFixer extends RedXmlConfigMarkerResolution {

    private final String pathOrName;

    private final boolean isPath;

    public AddLibraryToRedXmlFixer(final String pathOrName, final boolean isPath) {
        this.pathOrName = pathOrName;
        this.isPath = isPath;
    }

    @Override
    public String getLabel() {
        return "Discover '" + pathOrName + "' and add to configuration";
    }

    @Override
    protected ICompletionProposal asContentProposal(final IMarker marker, final IFile externalFile) {
        return new AddLibraryProposal(marker, externalFile, getLabel());
    }

    private class AddLibraryProposal extends RedConfigFileCompletionProposal {

        public AddLibraryProposal(final IMarker marker, final IFile externalFile, final String shortDescritption) {
            super(marker, externalFile, shortDescritption, null);
        }

        @Override
        public boolean apply(final IFile externalFile) throws ProposalApplyingException {
            final String libName;
            final String libPath;
            if (isPath) {
                libPath = pathOrName;
                libName = "someName";
            } else {
                final RobotProject project = RedPlugin.getModelManager().createProject(externalFile.getProject());
                final RobotRuntimeEnvironment env = project.getRuntimeEnvironment();
                
                final Optional<File> modulePath = env.getModulePath(pathOrName);
                if (modulePath.isPresent()) {
                    libName = pathOrName;
                    libPath = modulePath.get().getPath();
                } else {
                    MessageDialog.openError(Display.getCurrent().getActiveShell(), "Library import problem",
                            "Unable to locate '" + pathOrName + "' module. It seems that it is not contained"
                                    + " in PYTHONPATH of " + env.getFile() + " python installation.");
                    return false;
                }
            }

            try {
                final RobotProjectConfigReader reader = new RobotProjectConfigReader();
                final RobotProjectConfig config = reader.readConfiguration(externalFile);
                config.addReferencedLibraryInPython(libName, new Path(libPath));

                final RobotProjectConfigWriter writer = new RobotProjectConfigWriter();
                writer.writeConfiguration(config, externalFile.getProject());
            } catch (final CannotReadProjectConfigurationException | CannotWriteProjectConfigurationException e) {
                throw new ProposalApplyingException("Unable to apply proposal", e);
            }
            return true;
        }

        @Override
        public String getAdditionalProposalInfo() {
            return isPath ? "Add '" + pathOrName + "' location to red.xml file"
                    : "Try to discover location of '" + pathOrName + "' library and add it to red.xml file";
        }
    }
}
