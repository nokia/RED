/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.console;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ColorsManager;


/**
 * @author Michal Anglart
 *
 */
public class RedSessionConsole extends MessageConsole {

    private final Process process;
    private MessageConsoleStream outStream;
    private MessageConsoleStream errStream;

    RedSessionConsole(final String name, final Process process) {
        super(name, RedImages.getRobotImage());
        this.process = process;
    }

    void initializeStreams() {
        outStream = newMessageStream();
        errStream = newMessageStream();
        errStream.setColor(ColorsManager.getColor(255, 0, 0));
        errStream.setActivateOnWrite(true);
    }

    Process getProcess() {
        return process;
    }

    MessageConsoleStream getStdOutStream() {
        return outStream;
    }

    MessageConsoleStream getStdErrStream() {
        return errStream;
    }

    void processTerminated() {
        setName("<terminated> " + getName());
    }

    boolean isTerminated() {
        return !process.isAlive();
    }

    boolean isActivatingOnStdOutChange() {
        return outStream.isActivateOnWrite();
    }

    void setActivateOnStdOutChange(final boolean activate) {
        outStream.setActivateOnWrite(activate);
    }

    boolean isActivatingOnStdErrChange() {
        return errStream.isActivateOnWrite();
    }

    void setActivateOnStdErrChange(final boolean activate) {
        errStream.setActivateOnWrite(activate);
    }
}
