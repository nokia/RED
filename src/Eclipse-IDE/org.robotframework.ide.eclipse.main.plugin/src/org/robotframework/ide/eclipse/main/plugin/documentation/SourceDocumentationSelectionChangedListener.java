/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.documentation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
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

    private int offset;

    private DocViewDelayedUpdateJob delayedUpdateJob = new DocViewDelayedUpdateJob(
            "Documentation View Delayed Update Job");

    public SourceDocumentationSelectionChangedListener(final DocumentationView view) {
        this.view = view;
    }

    public void positionChanged(final IDocument document, final RobotSuiteFile suiteFile, final int offset,
            final boolean shouldRefreshCurrentDoc) {
        if (delayedUpdateJob.getState() == Job.RUNNING) {
            return;
        }

        if (delayedUpdateJob.getState() == Job.SLEEPING) {
            this.document = document;
            this.suiteFile = suiteFile;
            this.offset = offset;
            return;
        }

        this.document = document;
        this.suiteFile = suiteFile;
        this.offset = offset;

        if (shouldRefreshCurrentDoc) {
            view.resetCurrentlyDisplayedElement();
        }

        delayedUpdateJob.schedule(DocViewDelayedUpdateJob.DELAY);
    }

    class DocViewDelayedUpdateJob extends Job {

        public static final int DELAY = 700;

        public DocViewDelayedUpdateJob(final String name) {
            super(name);
            setUser(true);
        }

        @Override
        protected IStatus run(final IProgressMonitor monitor) {
            RobotFile linkedFile = suiteFile.getLinkedElement();
            if (linkedFile == null && document != null) {
                linkedFile = ((RobotDocument) document).getNewestModel();
            }
            if (linkedFile != null) {
                final Optional<IDocumentationHolder> docToShow = linkedFile.getParent()
                        .findDocumentationForOffset(offset);

                if (docToShow.isPresent()) {
                    view.showDocumentation(docToShow.get(), suiteFile);
                }
            }

            return Status.OK_STATUS;
        }
    }

}
