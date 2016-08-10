package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.RobotParser.RobotParserConfig;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;

public class RobotDocument extends Document {

    private static final int LIMIT = 50000;

    private boolean hasNewestVersion = false;
    private final Semaphore parsingSemaphore = new Semaphore(1);

    private Job job;

    private final Supplier<RobotSuiteFile> fileModelSupplier;
    private RobotParser parser;
    private File file;

    private RobotFileOutput output;

    private final List<IRobotDocumentParsingListener> parseListeners = new ArrayList<>();

    public RobotDocument(final Supplier<RobotSuiteFile> fileModelSupplier) {
        this.fileModelSupplier = fileModelSupplier;
    }

    @VisibleForTesting
    public RobotDocument(final RobotParser parser, final File file) {
        this.fileModelSupplier = new Supplier<RobotSuiteFile>() {
            @Override
            public RobotSuiteFile get() {
                return null;
            }
        };
        this.parser = parser;
        this.file = file;
    }

    public void addParseListener(final IRobotDocumentParsingListener listener) {
        parseListeners.add(listener);
    }

    public void removeParseListener(final IRobotDocumentParsingListener listener) {
        parseListeners.remove(listener);
    }

    @Override
    protected void fireDocumentAboutToBeChanged(final DocumentEvent event) {
        createParserIfNeeded();

        try {
            parsingSemaphore.acquire();
            hasNewestVersion = false;
        } catch (final InterruptedException e) {
            throw new IllegalStateException("Document reparsing interrupted!", e);
        }
        super.fireDocumentAboutToBeChanged(event);
    }

    private void createParserIfNeeded() {
        if (parser == null) {
            parser = createParser(fileModelSupplier.get());
            file = new File(fileModelSupplier.get().getName());
        }
    }

    @Override
    protected void fireDocumentChanged(final DocumentEvent event) {
        if (getLength() < LIMIT) {
            // short documents can be reparsed in the same thread as this does not
            // affect performance too much
            reparse();
        } else {
            reparseInSeparateThread();
        }
        super.fireDocumentChanged(event);
    }

    private void reparse() {
        output = parser.parseEditorContent(get(), file);
        for (final IRobotDocumentParsingListener listener : parseListeners) {
            listener.reparsingFinished(output);
        }
        hasNewestVersion = true;
        parsingSemaphore.release();
    }

    private void reparseInSeparateThread() {
        if (job != null && job.getState() == Job.SLEEPING) {
            job.cancel();
        }
        job = new Job("Document reparsing") {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                reparse();
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.schedule(150);
    }

    public Future<RobotFileOutput> getNewestOutput() {
        return new Future<RobotFileOutput>() {

            @Override
            public boolean cancel(final boolean mayInterruptIfRunning) {
                // not supported
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return hasNewestVersion;
            }

            @Override
            public RobotFileOutput get() throws InterruptedException, ExecutionException {
                parsingSemaphore.acquire();
                try {
                    return output;
                } finally {
                    parsingSemaphore.release();
                }
            }

            @Override
            public RobotFileOutput get(final long timeout, final TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                throw new IllegalStateException("Operation not supported");
            }
        };
    }

    /**
     * Gets newest parsed model. Waits for reparsing end if needed. IllegalStateException is thrown
     * when waiting has been interrupted.
     * 
     * @return
     */
    public RobotFile getNewestModel() {
        try {
            return getNewestOutput().get().getFileModel();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Waiting for newest model has been interrupted", e);
        }
    }

    private static RobotParser createParser(final RobotSuiteFile model) {
        final RobotParserConfig parserCfg = new RobotParserConfig();
        parserCfg.setEagerImport(false);
        parserCfg.setIncludeImportVariables(false);

        final RobotProjectHolder holder = model.getFile() == null ? new RobotProjectHolder()
                : model.getProject().getRobotProjectHolder();
        return RobotParser.create(holder, parserCfg);
    }
    
    public static interface IRobotDocumentParsingListener {

        void reparsingFinished(RobotFileOutput parsedOutput);
    }
}
