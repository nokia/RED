/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.validation;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;

/**
 * @author Michal Anglart
 */
class Logger extends BuildLogger {

    @Override
    public void log(final String message) {
        System.out.println(getTimestamp() + " " + message);
    }

    public void logError(final String message) {
        System.out.println(getTimestamp() + " " + message);
    }

    private String getTimestamp() {
        return new SimpleDateFormat("[HH:mm:ss.SSS]").format(new Date());
    }
}
