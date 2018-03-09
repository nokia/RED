/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.watcher;

/**
 * @author mmarzec
 */
public interface IWatchEventHandler {

    void handleModifyEvent(final String fileName);

    void watchServiceInterrupted();
}
