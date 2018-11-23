/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.environment.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.SourceOpeningSupport;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.dialogs.DetailedErrorDialog;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.swt.SwtThread.Evaluation;

import com.google.common.base.Charsets;

public class ConvertToRobotFileFormat extends RedSuiteMarkerResolution {

    private final File localFile;

    public ConvertToRobotFileFormat(final File localFile) {
        this.localFile = localFile;
    }

    @Override
    public String getLabel() {
        return "Convert file to 'robot' format using Tidy";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        final IResource file = marker.getResource();
        final IContainer parent = file.getParent();
        final RobotProject project = RedPlugin.getModelManager().createProject(file.getProject());
        final RobotRuntimeEnvironment runtimeEnvironment = project.getRuntimeEnvironment();

        return Optional.of(new FormatConversionProposal(runtimeEnvironment, parent, localFile));
    }

    private class FormatConversionProposal implements ICompletionProposal {

        private final RobotRuntimeEnvironment runtimeEnvironment;

        private final IContainer parentResource;

        private final File originalFile;

        public FormatConversionProposal(final RobotRuntimeEnvironment runtimeEnvironment,
                final IContainer parentResource, final File originalFile) {
            this.runtimeEnvironment = runtimeEnvironment;
            this.parentResource = parentResource;
            this.originalFile = originalFile;
        }

        @Override
        public void apply(final IDocument document) {
            final Job conversionJob = Job
                    .create("Converting '" + originalFile.getAbsolutePath() + "' to 'robot' format", monitor -> {
                        final String newContent = runtimeEnvironment.convertRobotDataFile(originalFile);
                        final IFile targetResource = parentResource.getFile(new Path(getTargetFileName()));

                        final boolean written = writeNewContentToTargetFile(newContent, targetResource);
                        if (written && targetResource.exists()) {
                            openTargetFile(targetResource);
                        }
                    });
            conversionJob.schedule();
        }

        protected boolean writeNewContentToTargetFile(final String newContent, final IFile targetResource)
                throws CoreException {
            try (InputStream input = new ByteArrayInputStream(newContent.getBytes(Charsets.UTF_8))) {
                return targetResource.exists()
                        ? overrideConvertedFileIfAgreed(targetResource, input)
                        : createConvertedFile(targetResource, input);

            } catch (final IOException e) {
                DetailedErrorDialog.openErrorDialog(
                        "Unable to write to '" + targetResource.getLocation().toString() + "' file", e.getMessage());
                return false;
            }
        }

        private boolean overrideConvertedFileIfAgreed(final IFile targetResource, final InputStream input)
                throws CoreException {
            final boolean shouldOverride = SwtThread.syncEval(Evaluation.of(() -> {
                final Shell shell = Display.getCurrent().getActiveShell();
                return MessageDialog.openConfirm(shell, "File already exist",
                        "The file '" + targetResource.getLocation().toString()
                                + "' already exist. It's content will be overridden.");
            }));
            if (shouldOverride) {
                targetResource.setContents(input, true, true, null);
                return true;
            }
            return false;
        }

        private boolean createConvertedFile(final IFile targetResource, final InputStream input) throws CoreException {
            targetResource.create(input, true, null);
            return true;
        }

        private void openTargetFile(final IFile targetResource) {
            SwtThread.asyncExec(() -> {
                final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                SourceOpeningSupport.tryToOpenInEditor(page, targetResource);
            });
        }

        @Override
        public Point getSelection(final IDocument document) {
            return null;
        }

        @Override
        public String getAdditionalProposalInfo() {
            final String targetFileName = getTargetFileName();
            return "The file will be converted to " + targetFileName
                    + " using Tidy built-in tool from Robot Framework. The original file will be preserved.";
        }

        private String getTargetFileName() {
            return originalFile.getName().substring(0, originalFile.getName().lastIndexOf('.')) + ".robot";
        }

        @Override
        public String getDisplayString() {
            return getLabel();
        }

        @Override
        public Image getImage() {
            return ImagesManager.getImage(RedImages.getRobotFileImage());
        }

        @Override
        public IContextInformation getContextInformation() {
            return null;
        }
    }
}
