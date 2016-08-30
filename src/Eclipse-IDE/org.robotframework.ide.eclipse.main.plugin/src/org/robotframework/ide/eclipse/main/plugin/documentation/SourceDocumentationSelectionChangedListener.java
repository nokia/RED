/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.documentation;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.RobotFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RobotDocument;
import org.robotframework.ide.eclipse.main.plugin.views.DocumentationView;

import com.google.common.base.Optional;

public class SourceDocumentationSelectionChangedListener {

    private DocumentationView view;

    private RobotSuiteFile suiteFile;

    private IDocument document;

    private IRegion currentRegion;

    private AtomicBoolean isEditing = new AtomicBoolean();

    private static final Pattern possibleKeywordPattern = Pattern.compile("^[A-Za-z].*");

    private DocViewDelayedUpdateJob delayedUpdateJob = new DocViewDelayedUpdateJob(
            "Documentation View Delayed Update Job");

    public SourceDocumentationSelectionChangedListener(final DocumentationView view) {
        this.view = view;
    }

    public void positionChanged(final IDocument document, final RobotSuiteFile suiteFile, final IRegion region,
            final boolean isEditing) {

        if (delayedUpdateJob.getState() == Job.SLEEPING) {
            delayedUpdateJob.cancel();
        }

        this.document = document;
        this.suiteFile = suiteFile;
        this.currentRegion = region;
        this.isEditing.set(isEditing);

        delayedUpdateJob.schedule(DocViewDelayedUpdateJob.DELAY);
    }

    class DocViewDelayedUpdateJob extends Job {

        public static final int DELAY = 700;

        public DocViewDelayedUpdateJob(final String name) {
            super(name);
            setSystem(true);
        }

        @Override
        protected IStatus run(final IProgressMonitor monitor) {
            RobotFile linkedFile = suiteFile.getLinkedElement();
            if (linkedFile == null && document != null) {
                linkedFile = ((RobotDocument) document).getNewestModel();
            }

            if (linkedFile != null) {
                final Optional<IDocumentationHolder> docSettingToShow = linkedFile.getParent()
                        .findDocumentationForOffset(currentRegion.getOffset());

                if (docSettingToShow.isPresent()) {
                    if (isEditing.get()) {
                        view.resetCurrentlyDisplayedElement();
                    }
                    view.showDocumentation(docSettingToShow.get(), suiteFile);

                } else if (shouldTryToFindKeywordDoc()) {
                    try {
                        final String textFromCurrentRegion = document.get(currentRegion.getOffset(),
                                currentRegion.getLength());
                        if (isPossibleKeyword(textFromCurrentRegion)) {
                            view.showLibdoc(textFromCurrentRegion, suiteFile);
                        }
                    } catch (final BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            }

            return Status.OK_STATUS;
        }

        private boolean shouldTryToFindKeywordDoc() {
            return currentRegion != null && view.hasShowLibdocEnabled() && !isEditing.get();
        }

        private boolean isPossibleKeyword(final String text) {
            return possibleKeywordPattern.matcher(text).matches();
        }
    }

}
