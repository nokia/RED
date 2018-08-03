/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.RobotParser.RobotParserConfig;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;

public class RobotDocument extends Document {

    private static final int DELAY = 200;
    private static final int LIMIT = 500;

    private final AtomicBoolean hasNewestVersion = new AtomicBoolean(false);
    private final Semaphore parsingSemaphore = new Semaphore(1, true);
    private final Semaphore parsingFinishedSemaphore = new Semaphore(1, true);
    private boolean reparseInSameThread = true;

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    private final Supplier<RobotSuiteFile> fileModelSupplier;
    private RobotParser parser;
    private File file;

    private RobotFileOutput output;

    private final List<IRobotDocumentParsingListener> parseListeners = new ArrayList<>();
    private ScheduledFuture<?> scheduledOperation;

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

    public void addFirstDocumentListener(final IDocumentListener documentListener) {
        Assert.isNotNull(documentListener);

        final List<IDocumentListener> allListeners = new ArrayList<>();
        allListeners.add(documentListener);

        for (final Object listener : getDocumentListeners()) {
            allListeners.add((IDocumentListener) listener);
            removeDocumentListener((IDocumentListener) listener);
        }
        for (final IDocumentListener listener : allListeners) {
            addDocumentListener(listener);
        }
    }

    public void addParseListener(final IRobotDocumentParsingListener listener) {
        parseListeners.add(listener);
    }

    public void removeParseListener(final IRobotDocumentParsingListener listener) {
        parseListeners.remove(listener);
    }

    public boolean hasNewestModel() {
        return hasNewestVersion.get();
    }

    @Override
    protected void fireDocumentAboutToBeChanged(final DocumentEvent event) {
        createParserIfNeeded();
        reparseInSameThread = getNumberOfLines() < LIMIT;
        if (!reparseInSameThread & hasNewestVersion.getAndSet(false)) {
            try {
                // it will be acquired only by the first event
                parsingSemaphore.acquire();
            } catch (final InterruptedException e) {
                throw new IllegalStateException("Document reparsing interrupted!", e);
            }
        }
        super.fireDocumentAboutToBeChanged(event);
    }

    private void createParserIfNeeded() {
        if (parser == null) {
            final RobotSuiteFile suiteFile = fileModelSupplier.get();
            parser = createParser(suiteFile);
            file = suiteFile.getFile() == null ? new File(suiteFile.getName())
                    : suiteFile.getFile().getLocation().toFile();
            reparse();
        }
    }

    @Override
    protected void fireDocumentChanged(final DocumentEvent event) {
        if (reparseInSameThread) {
            // short documents can be reparsed in the same thread as this does not
            // affect performance too much
            reparse();
        } else {
            reparseInSeparateThread();
        }
        super.fireDocumentChanged(event);
    }

    private synchronized void reparse() {
        output = parser.parseEditorContent(get(), file);
        for (final IRobotDocumentParsingListener listener : parseListeners) {
            listener.reparsingFinished(output);
        }
        hasNewestVersion.set(true);
    }

    private void reparseInSeparateThread() {
        if (scheduledOperation != null) {
            scheduledOperation.cancel(true);
        }
        scheduledOperation = executor.schedule(() -> {
            reparse();
            parsingSemaphore.release();
        }, DELAY, TimeUnit.MILLISECONDS);
    }

    private Future<RobotFileOutput> getNewestOutput() {
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
                return hasNewestVersion.get();
            }

            @Override
            public RobotFileOutput get() throws InterruptedException, ExecutionException {
                // we don't want situation, when two threads acquire parsingSemaphore and parsing
                // task cannot be performed
                parsingFinishedSemaphore.acquire();
                parsingSemaphore.acquire();
                try {
                    return output;
                } finally {
                    parsingSemaphore.release();
                    parsingFinishedSemaphore.release();
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
     * @throws InterruptedException
     */
    public RobotFile getNewestModel() throws InterruptedException {
        final RobotFileOutput newestFileOutput = getNewestFileOutput();
        return newestFileOutput == null ? new RobotFile(null) : newestFileOutput.getFileModel();
    }

    /**
     * Gets newest parsed file output. Waits for reparsing end if needed. IllegalStateException is
     * thrown when waiting has been interrupted.
     * 
     * @return
     * @throws InterruptedException
     */
    public RobotFileOutput getNewestFileOutput() throws InterruptedException {
        try {
            return getNewestOutput().get();
        } catch (final ExecutionException e) {
            throw new IllegalStateException("Parsing the file coulnd't be finished", e.getCause());
        }
    }

    private static RobotParser createParser(final RobotSuiteFile model) {
        final RobotVersion version = model.getRobotVersion();
        final RobotProjectHolder holder = isNonFileModel(model) ? new RobotProjectHolder()
                : model.getProject().getRobotProjectHolder();
        final PathsProvider pathsProvider = isNonFileModel(model) ? null : model.getProject().createPathsProvider();
        return RobotParser.create(holder, RobotParserConfig.allImportsLazy(version), pathsProvider);
    }
    
    private static boolean isNonFileModel(final RobotSuiteFile model) {
        // e.g. history revision
        return model.getFile() == null;
    }

    public static interface IRobotDocumentParsingListener {

        void reparsingFinished(RobotFileOutput parsedOutput);
    }
}
