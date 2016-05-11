/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.fileWatcher;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author mmarzec
 */
public class RedFileWatcher {

    private static class InstanceHolder {

        private static final RedFileWatcher INSTANCE = new RedFileWatcher();
    }

    public static RedFileWatcher getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private WatchService watcher;

    private List<String> registeredDirs = new ArrayList<>();

    private Map<String, Collection<IWatchEventHandler>> registeredFiles = Collections
            .synchronizedMap(new HashMap<String, Collection<IWatchEventHandler>>());

    private LinkedBlockingQueue<String> modifiedFilesQueue = new LinkedBlockingQueue<>(100);

    private AtomicBoolean isEventProducerThreadStarted = new AtomicBoolean(false);

    private AtomicBoolean isEventConsumerThreadStarted = new AtomicBoolean(false);

    private RedFileWatcher() {}

    public synchronized void registerPath(final Path fileDir, final String fileName,
            final IWatchEventHandler watchEventHandler) {

        if (watcher == null) {
            setupWatcher();
        }

        if (watcher != null && fileDir != null && fileName != null) {
            try {
                if (!registeredDirs.contains(fileDir.toString()) && isEventProducerThreadStarted.get()
                        && isEventConsumerThreadStarted.get()) {
                    fileDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
                    registeredDirs.add(fileDir.toString());
                }
                final Collection<IWatchEventHandler> handlers = registeredFiles.get(fileName);
                if (handlers == null) {
                    registeredFiles.put(fileName, newArrayList(watchEventHandler));
                } else {
                    if (!handlers.contains(watchEventHandler)) {
                        handlers.add(watchEventHandler);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void unregisterFile(final String fileName, final IWatchEventHandler watchEventHandler) {
        if (fileName != null) {
            final Collection<IWatchEventHandler> handlers = registeredFiles.get(fileName);
            if (handlers != null && !handlers.isEmpty()) {
                if (handlers.size() == 1 && handlers.contains(watchEventHandler)) {
                    registeredFiles.remove(fileName);
                } else {
                    handlers.remove(watchEventHandler);
                }
            }
        }
    }
    
    public synchronized void closeWatchService() {
        try {
            if (watcher != null) {
                watcher.close();
            }
        } catch (IOException e) {
            // nothing to do
        } finally {
            watcher = null;
        }
    }

    private void setupWatcher() {
        registeredDirs.clear();
        modifiedFilesQueue.clear();
        createAndStartWatchService();
    }

    private void createAndStartWatchService() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
            startWatching();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startWatching() {
        startEventProducerThread();
        startEventConsumerThread();
    }

    private void startEventProducerThread() {
        if (!isEventProducerThreadStarted.getAndSet(true)) {
            new Thread(new Runnable() {

                @Override
                public void run() {

                    while (true) {
                        if (watcher == null) {
                            break;
                        }
                        WatchKey key;
                        try {
                            key = watcher.take();
                        } catch (InterruptedException | ClosedWatchServiceException e) {
                            break;
                        }
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            final Path fileNameFromEvent = ev.context();

                            if (kind == StandardWatchEventKinds.OVERFLOW) {
                                continue;
                            } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {

                                final String fileName = fileNameFromEvent.getFileName().toString();
                                if (registeredFiles.get(fileName) != null) {
                                    try {
                                        if (!modifiedFilesQueue.contains(fileName)) {
                                            modifiedFilesQueue.put(fileName);
                                        }
                                    } catch (InterruptedException e) {
                                        break;
                                    }
                                }
                            }
                        }
                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }
                    }
                    isEventProducerThreadStarted.set(false);
                    closeWatchService();
                    sendWatchServiceInterruptedEvent();
                }
            }).start();
        }
    }

    private void startEventConsumerThread() {
        if (!isEventConsumerThreadStarted.getAndSet(true)) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        try {
                            final String fileName = modifiedFilesQueue.take();
                            final Collection<IWatchEventHandler> eventHandlers = registeredFiles.get(fileName);
                            if (eventHandlers != null && !eventHandlers.isEmpty()) {
                                for (IWatchEventHandler watchEventHandler : eventHandlers) {
                                    watchEventHandler.handleModifyEvent(fileName);
                                }
                            }
                            waitForAndRemovePossibleDuplicatedEvents(fileName);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    isEventConsumerThreadStarted.set(false);
                    closeWatchService();
                    sendWatchServiceInterruptedEvent();
                }
            }).start();
        }
    }

    private void waitForAndRemovePossibleDuplicatedEvents(final String fileName) {
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            return;
        }
        String nextFileName = modifiedFilesQueue.peek();
        if (nextFileName != null && nextFileName.equals(fileName)) {
            modifiedFilesQueue.remove();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                return;
            }
            nextFileName = modifiedFilesQueue.peek();
            if (nextFileName != null && nextFileName.equals(fileName)) {
                modifiedFilesQueue.remove();
            }
        }
    }

    private void sendWatchServiceInterruptedEvent() {
        for (final String file : registeredFiles.keySet()) {
            final Collection<IWatchEventHandler> eventHandlers = registeredFiles.get(file);
            if (eventHandlers != null && !eventHandlers.isEmpty()) {
                for (final IWatchEventHandler handler : eventHandlers) {
                    handler.watchServiceInterrupted();
                }
            }
        }
    }
}
