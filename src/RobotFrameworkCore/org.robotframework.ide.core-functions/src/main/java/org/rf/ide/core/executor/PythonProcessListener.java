/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;


/**
 * @author Michal Anglart
 *
 */
public interface PythonProcessListener {

    void processStarted(String name, Process process);

    void processEnded(Process process);

    void lineRead(Process serverProcess, String line);

    void errorLineRead(Process serverProcess, String line);

}
