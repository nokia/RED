/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.validation;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;

/**
 * @author Michal Anglart
 */
public class Logger extends BuildLogger {

    @Override
    public void log(final String message) {
        System.out.println(getTimestamp() + " " + message);
    }

    @Override
    public void logError(final String message, final Throwable cause) {
        System.err.println(getTimestamp() + " " + message);
        if (cause != null) {
            RedPlugin.logError(message, cause);
        }
    }

    private String getTimestamp() {
        return new SimpleDateFormat("[HH:mm:ss.SSS]").format(new Date());
    }
}
