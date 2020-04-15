/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.environment;


/**
 * @author Michal Anglart
 *
 */
public interface PythonProcessListener {

    void processStarted(String interpreterPath, Process process);

    void processEnded(Process process);

    void lineRead(Process process, String line);

    void errorLineRead(Process process, String line);

}
