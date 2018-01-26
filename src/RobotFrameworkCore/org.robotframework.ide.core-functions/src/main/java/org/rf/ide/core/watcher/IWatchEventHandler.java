/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.watcher;

import java.nio.file.Path;

/**
 * @author mmarzec
 */
public interface IWatchEventHandler {

    void registerPath(final Path dir, final String fileName, final IWatchEventHandler handler);

    void unregisterFile(final String fileName, final IWatchEventHandler handler);

    void watchServiceInterrupted();

    void handleModifyEvent(final String fileName);
}
