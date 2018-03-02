/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;

import com.google.common.base.Strings;

/**
 * @author Michal Anglart
 *
 */
public abstract class RedSuiteMarkerResolution implements IMarkerResolution {

    @Override
    public final void run(final IMarker marker) {
        final IFile file = (IFile) marker.getResource();

        final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
        final IEditorDescriptor desc = editorRegistry.findEditor(RobotFormEditor.ID);
        try {
            final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            final IEditorPart ed = page.openEditor(new FileEditorInput(file), desc.getId());
            if (ed instanceof RobotFormEditor) { // it can be ErrorEditorPart if something went
                                                 // wrong
                final RobotFormEditor editor = (RobotFormEditor) ed;
                final SuiteSourceEditor activatedPage = editor.activateSourcePage();
                activatedPage.setFocus();

                final IDocument document = activatedPage.getDocument();
                final RobotSuiteFile model = editor.provideSuiteModel();

                final Optional<ICompletionProposal> asContentProposal = asContentProposal(marker, document, model);
                if (asContentProposal.isPresent()) {
                    asContentProposal.get().apply(document);
                    marker.delete();
                }
            }
        } catch (final CoreException e) {
            // oh well, we won't fix this...
        }
    }

    protected final String getLineDelimiter(final IDocument document) {
        try {
            final String delimiter = document.getLineDelimiter(0);
            return delimiter == null ? System.lineSeparator() : delimiter;
        } catch (final BadLocationException e) {
            return System.lineSeparator();
        }
    }

    protected final String getSeparator(final RobotSuiteFile suiteModel, final int offset) {
        final RobotFile fileModel = suiteModel.getLinkedElement();
        final FileFormat fileFormat = fileModel.getParent().getFileFormat();
        if (fileFormat == FileFormat.TSV) {
            return "\t";
        } else {
            return fileModel.getRobotLineIndexBy(offset)
                    .map(l -> fileModel.getFileContent().get(l))
                    .flatMap(RobotLine::getSeparatorForLine)
                    .filter(sep -> sep == SeparatorType.PIPE)
                    .map(sep -> " | ")
                    .orElse(Strings.repeat(" ", 4));
        }
    }

    protected final String getSeparator(final RobotSuiteFile suiteModel) {
        final RobotFile fileModel = suiteModel.getLinkedElement();
        final FileFormat fileFormat = fileModel.getParent().getFileFormat();
        return fileFormat == FileFormat.TSV ? "\t" : Strings.repeat(" ", 4);
    }

    public abstract Optional<ICompletionProposal> asContentProposal(IMarker marker, IDocument document,
            RobotSuiteFile suiteModel);

}
